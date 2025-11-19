package com.example.secondhandmarket.database

import android.util.Log
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

/**
 * MySQL数据库连接类
 * 实现单例模式和连接池管理
 * 参考Java实例进行优化
 */
class MySQLConnections private constructor() {
    
    companion object {
        private const val TAG = "MySQLConnections"
        
        // 数据库配置
        private const val DRIVER = "com.mysql.jdbc.Driver"
        private const val DB_URL = "jdbc:mysql://rm-bp15zc4nz3pyyi4512o.mysql.rds.aliyuncs.com:3306/market"
        private const val USER = "dbuser"
        private const val PASSWORD = "Market123"
        
        // 单例实例
        private var instance: MySQLConnections? = null
        
        /**
         * 获取数据库连接实例
         */
        fun getConnection(): Connection? {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        try {
                            instance = MySQLConnections()
                            Log.d(TAG, "MySQLConnections实例创建成功")
                            Log.d(TAG, "数据库URL: $DB_URL")
                        } catch (e: Exception) {
                            Log.e(TAG, "创建MySQLConnections实例失败", e)
                            return null
                        }
                    }
                }
            }
            
            return try {
                // 加载MySQL驱动
                Class.forName(DRIVER)
                
                // 创建数据库连接
                val conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)
                Log.d(TAG, "数据库连接创建成功")
                conn
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "MySQL JDBC驱动未找到", e)
                null
            } catch (e: SQLException) {
                Log.e(TAG, "数据库连接异常", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "获取数据库连接时发生未知异常", e)
                null
            }
        }
        
        /**
         * 测试数据库连接
         */
        fun testConnection(): Boolean {
            return try {
                val conn = getConnection()
                val isValid = conn?.isValid(5) ?: false
                conn?.close()
                isValid
            } catch (e: Exception) {
                Log.e(TAG, "测试数据库连接失败", e)
                false
            }
        }
        
        /**
         * 关闭数据库连接
         */
        fun closeConnection(connection: Connection?) {
            try {
                connection?.close()
                Log.d(TAG, "数据库连接已关闭")
            } catch (e: SQLException) {
                Log.e(TAG, "关闭数据库连接时发生异常", e)
            }
        }
    }
    
    init {
        Log.d(TAG, "MySQLConnections构造函数被调用")
    }
}