package com.example.secondhandmarket.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/**
 * 数据库管理器
 * 负责管理MySQL数据库连接和SQL操作
 * 优化版本：集成MySQLConnections类，实现更好的连接管理
 */
class DatabaseManager private constructor() {

    companion object {
        private var instance: DatabaseManager? = null
        
        // 重试配置
        private const val MAX_RETRY_COUNT = 3
        private const val RETRY_INTERVAL_MS = 5000L
        
        // 日志标签
        private const val TAG = "DatabaseManager"
        
        fun getInstance(): DatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: DatabaseManager().also { instance = it }
            }
        }
    }
    
    // 数据库连接对象
    private var connection: Connection? = null
    
    // 连接状态标志
    private var connectionFailed = false
    
    /**
     * 初始化数据库配置
     */
    fun initializeConfig() {
        Log.d(TAG, "开始数据库配置初始化")
        Log.d(TAG, "使用MySQLConnections类进行数据库连接")
    }
    
    /**
     * 初始化数据库连接
     */
    suspend fun initialize(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "开始数据库连接初始化")
                
                // 加载MySQL驱动
                Class.forName("com.mysql.jdbc.Driver")
                
                var connected = false
                var retryCount = 0
                
                while (retryCount <= Companion.MAX_RETRY_COUNT && !connected) {
                    retryCount++
                    
                    Log.i(TAG, "Attempting database connection (attempt $retryCount)...")
                    
                    connected = tryConnectWithRetry()
                    
                    if (!connected && retryCount < Companion.MAX_RETRY_COUNT) {
                        Log.w(TAG, "Connection attempt $retryCount failed, retrying in ${Companion.RETRY_INTERVAL_MS/1000} seconds...")
                        kotlinx.coroutines.delay(Companion.RETRY_INTERVAL_MS)
                    }
                }
                
                if (connected) {
                    Log.i(TAG, "Database connection successful after $retryCount attempts")
                    return@withContext true
                } else {
                    Log.w(TAG, "数据库连接失败，已达到最大重试次数（${Companion.MAX_RETRY_COUNT}）")
                    Log.i(TAG, "将使用本地模拟数据模式")
                    connectionFailed = true
                    return@withContext false
                }
                
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "MySQL JDBC驱动未找到", e)
                Log.i(TAG, "将使用本地模拟数据模式")
                connectionFailed = true
                return@withContext false
            } catch (e: SQLException) {
                Log.e(TAG, "MySQL数据库连接异常", e)
                Log.i(TAG, "数据库连接失败，将使用本地模拟数据模式")
                Log.i(TAG, "请检查数据库服务器是否运行且配置正确")
                connectionFailed = true
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "数据库初始化过程中出现未知异常", e)
                Log.i(TAG, "将使用本地模拟数据模式")
                connectionFailed = true
                return@withContext false
            }
        }
    }
    
    /**
     * 检查数据库连接是否有效
     */
    private fun isConnectionValid(): Boolean {
        return try {
            connection != null && !connection!!.isClosed && connection!!.isValid(5)
        } catch (e: SQLException) {
            false
        }
    }
    
    /**
     * 获取数据库连接
     * 优化版本：集成MySQLConnections类，实现更好的连接管理
     */
    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        // 如果连接已失败且未启用重试，直接返回null
        if (connectionFailed && MAX_RETRY_COUNT <= 0) {
            Log.w(TAG, "数据库连接已失败且未启用重试机制")
            return@withContext null
        }
        
        // 检查现有连接是否有效
        if (connection != null && connection!!.isValid(5)) {
            Log.d(TAG, "使用现有有效数据库连接")
            return@withContext connection
        }
        
        // 重试逻辑
        var retryCount = 0
        while (retryCount < MAX_RETRY_COUNT) {
            try {
                Log.d(TAG, "尝试连接数据库 (重试次数: ${retryCount + 1}/$MAX_RETRY_COUNT)")
                
                // 使用MySQLConnections类获取连接
                connection = MySQLConnections.getConnection()
                
                // 测试连接
                if (connection != null && connection!!.isValid(5)) {
                    Log.i(TAG, "数据库连接成功 (通过MySQLConnections)")
                    connectionFailed = false
                    return@withContext connection
                } else {
                    Log.w(TAG, "数据库连接无效")
                    MySQLConnections.closeConnection(connection)
                    connection = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "连接数据库时发生异常", e)
            }
            
            retryCount++
            
            // 如果不是最后一次重试，等待一段时间
            if (retryCount < MAX_RETRY_COUNT) {
                Log.d(TAG, "等待 ${RETRY_INTERVAL_MS}ms 后重试...")
                Thread.sleep(RETRY_INTERVAL_MS)
            }
        }
        
        // 所有重试都失败
        Log.e(TAG, "数据库连接失败，已达到最大重试次数: $MAX_RETRY_COUNT")
        connectionFailed = true
        null
    }
    
    /**
     * 如果表不存在则创建表
     */
    private suspend fun createTablesIfNotExists() {
        try {
            val conn = getConnection() ?: return
            
            // 创建商品表
            val createItemsTable = """
                CREATE TABLE IF NOT EXISTS items (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    title VARCHAR(255) NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    image_url VARCHAR(500),
                    description TEXT,
                    category VARCHAR(50) NOT NULL,
                    location VARCHAR(100),
                    seller_id BIGINT,
                    seller_name VARCHAR(100),
                    created_at DATE,
                    status VARCHAR(20) NOT NULL,
                    `condition` VARCHAR(20) NOT NULL,
                    brand VARCHAR(100),
                    views INT DEFAULT 0,
                    likes INT DEFAULT 0
                )
            """.trimIndent()
            
            // 创建卖家表
            val createSellersTable = """
                CREATE TABLE IF NOT EXISTS sellers (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(255) UNIQUE,
                    phone VARCHAR(20),
                    location VARCHAR(100),
                    rating DECIMAL(3,2) DEFAULT 0.0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """.trimIndent()
            
            conn.createStatement().use { statement ->
                statement.execute(createItemsTable)
                statement.execute(createSellersTable)
                Log.d(TAG, "数据库表创建成功")
            }
            
        } catch (e: SQLException) {
            Log.e(TAG, "创建数据库表失败", e)
        }
    }
    
    /**
     * 执行查询SQL
     */
    suspend fun executeQuery(sql: String, params: List<Any>? = null): ResultSet? {
        return withContext(Dispatchers.IO) {
            try {
                val conn = getConnection() ?: return@withContext null
                
                conn.prepareStatement(sql).use { preparedStatement ->
                    // 设置参数
                    params?.forEachIndexed { index, param ->
                        preparedStatement.setObject(index + 1, param)
                    }
                    
                    val resultSet = preparedStatement.executeQuery()
                    Log.d(TAG, "SQL查询执行成功: $sql")
                    return@withContext resultSet
                }
                
            } catch (e: SQLException) {
                Log.e(TAG, "SQL查询执行失败", e)
                return@withContext null
            }
        }
    }
    
    /**
     * 执行更新SQL（INSERT、UPDATE、DELETE）
     */
    suspend fun executeUpdate(sql: String, params: List<Any>? = null): Int {
        return withContext(Dispatchers.IO) {
            try {
                val conn = getConnection() ?: return@withContext 0
                
                conn.prepareStatement(sql).use { preparedStatement ->
                    // 设置参数
                    params?.forEachIndexed { index, param ->
                        preparedStatement.setObject(index + 1, param)
                    }
                    
                    val affectedRows = preparedStatement.executeUpdate()
                    Log.d(TAG, "SQL更新执行成功，影响行数: $affectedRows")
                    return@withContext affectedRows
                }
                
            } catch (e: SQLException) {
                Log.e(TAG, "SQL更新执行失败", e)
                return@withContext 0
            }
        }
    }
    

    
    /**
     * 测试数据库连接
     */
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            // 使用MySQLConnections的测试方法
            val isValid = MySQLConnections.testConnection()
            Log.d(TAG, "数据库连接测试结果: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "测试数据库连接失败", e)
            false
        }
    }

    /**
     * 关闭数据库连接
     */
    fun close() {
        try {
            MySQLConnections.closeConnection(connection)
            connection = null
            Log.d(TAG, "数据库连接已关闭")
        } catch (e: SQLException) {
            Log.e(TAG, "关闭数据库连接时发生异常", e)
        }
    }
    
    /**
     * 快速测试连接（不通过DatabaseManager的重试机制）
     */
    fun quickTestConnection(): Boolean {
        return MySQLConnections.testConnection()
    }
    
    // 私有辅助方法
    
    private suspend fun tryConnectWithRetry(): Boolean {
        return try {
            // 使用MySQLConnections类获取连接
            connection = MySQLConnections.getConnection()
            
            // 创建表
            createTablesIfNotExists()
            
            connection != null
        } catch (e: SQLException) {
            Log.e(TAG, "数据库连接失败: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "数据库连接异常: ${e.message}")
            false
        }
    }
}