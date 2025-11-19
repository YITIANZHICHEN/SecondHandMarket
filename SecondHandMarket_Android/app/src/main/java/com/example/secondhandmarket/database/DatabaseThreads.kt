package com.example.secondhandmarket.database

import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * 数据库操作线程类
 * 实现异步数据库操作，参考Java实例进行优化
 */
class DatabaseThreads {
    
    companion object {
        private const val TAG = "DatabaseThreads"
    }
    
    /**
     * 发送消息线程类
     * 实现点击按钮发送数据的功能
     */
    class SendMessageThread(
        private val activity: AppCompatActivity,
        private val messageCallback: (String) -> Unit,
        private val errorCallback: (String) -> Unit
    ) : Thread() {
        
        // 发送标志位
        private var shouldSend = false
        
        // 数据库连接和语句对象
        private var connection: Connection? = null
        private var statement: PreparedStatement? = null
        
        /**
         * 设置发送标志位
         */
        fun setShouldSend(shouldSend: Boolean) {
            this.shouldSend = shouldSend
        }
        
        override fun run() {
            while (true) {
                if (shouldSend) {
                    try {
                        // 获取数据库连接
                        connection = MySQLConnections.getConnection()
                        
                        if (connection != null) {
                            // 模拟消息发送（这里可以根据实际需求修改）
                            val message = "测试消息"
                            
                            // 插入消息到数据库（示例表结构）
                            val sql = "INSERT INTO messages (user_id, content, created_at) VALUES (?, ?, NOW())"
                            statement = connection?.prepareStatement(sql)
                            
                            // 设置参数
                            statement?.setInt(1, 1) // 用户ID
                            statement?.setString(2, message) // 消息内容
                            
                            // 关闭事务自动提交
                            connection?.autoCommit = false
                            
                            // 执行插入
                            val result = statement?.executeUpdate()
                            
                            if (result != null && result > 0) {
                                // 提交事务
                                connection?.commit()
                                
                                // 发送成功回调
                                activity.runOnUiThread {
                                    Toast.makeText(activity, "消息发送成功", Toast.LENGTH_SHORT).show()
                                    messageCallback("消息发送成功: $message")
                                }
                            } else {
                                // 回滚事务
                                connection?.rollback()
                                
                                activity.runOnUiThread {
                                    Toast.makeText(activity, "消息发送失败", Toast.LENGTH_SHORT).show()
                                    errorCallback("消息发送失败")
                                }
                            }
                        } else {
                            activity.runOnUiThread {
                                Toast.makeText(activity, "数据库连接失败", Toast.LENGTH_SHORT).show()
                                errorCallback("数据库连接失败")
                            }
                        }
                    } catch (e: SQLException) {
                        Log.e(TAG, "SQL异常", e)
                        
                        try {
                            connection?.rollback()
                        } catch (rollbackEx: SQLException) {
                            Log.e(TAG, "回滚事务失败", rollbackEx)
                        }
                        
                        activity.runOnUiThread {
                            Toast.makeText(activity, "数据库操作异常", Toast.LENGTH_SHORT).show()
                            errorCallback("数据库操作异常: ${e.message}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "发送消息时发生异常", e)
                        
                        activity.runOnUiThread {
                            Toast.makeText(activity, "发送消息失败", Toast.LENGTH_SHORT).show()
                            errorCallback("发送消息失败: ${e.message}")
                        }
                    } finally {
                        // 清理资源
                        try {
                            statement?.close()
                            MySQLConnections.closeConnection(connection)
                        } catch (e: SQLException) {
                            Log.e(TAG, "关闭数据库资源失败", e)
                        }
                        
                        // 重置发送标志位
                        shouldSend = false
                    }
                }
                
                // 短暂休眠，避免过度消耗CPU
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "发送线程被中断")
                    break
                }
            }
        }
        
        /**
         * 安全停止线程
         */
        fun safeStop() {
            interrupt()
        }
    }
    
    /**
     * 读取数据线程类
     * 实现每隔两秒获取数据库中数据的功能
     */
    class ReadDataThread(
        private val activity: AppCompatActivity,
        private val dataCallback: (List<String>) -> Unit,
        private val errorCallback: (String) -> Unit
    ) : Thread() {
        
        // 运行标志位
        private var isRunning = true
        
        // 数据库连接和语句对象
        private var connection: Connection? = null
        private var statement: PreparedStatement? = null
        
        override fun run() {
            while (isRunning) {
                try {
                    // 获取数据库连接
                    connection = MySQLConnections.getConnection()
                    
                    if (connection != null) {
                        // 查询数据（示例查询）
                        val sql = "SELECT id, user_id, content, created_at FROM messages ORDER BY created_at DESC LIMIT 10"
                        statement = connection?.prepareStatement(sql)
                        
                        // 关闭事务自动提交
                        connection?.autoCommit = false
                        
                        // 执行查询
                        val resultSet = statement?.executeQuery()
                        
                        val messages = mutableListOf<String>()
                        
                        // 处理结果集
                        while (resultSet?.next() == true) {
                            val id = resultSet.getInt("id")
                            val userId = resultSet.getInt("user_id")
                            val content = resultSet.getString("content")
                            val createdAt = resultSet.getTimestamp("created_at")
                            
                            messages.add("消息ID: $id, 用户ID: $userId, 内容: $content, 时间: $createdAt")
                        }
                        
                        // 提交事务
                        connection?.commit()
                        
                        // 回调数据
                        activity.runOnUiThread {
                            dataCallback(messages)
                        }
                        
                        // 关闭结果集
                        resultSet?.close()
                    } else {
                        activity.runOnUiThread {
                            errorCallback("数据库连接失败")
                        }
                    }
                } catch (e: SQLException) {
                    Log.e(TAG, "SQL异常", e)
                    
                    try {
                        connection?.rollback()
                    } catch (rollbackEx: SQLException) {
                        Log.e(TAG, "回滚事务失败", rollbackEx)
                    }
                    
                    activity.runOnUiThread {
                        errorCallback("数据库查询异常: ${e.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "读取数据时发生异常", e)
                    
                    activity.runOnUiThread {
                        errorCallback("读取数据失败: ${e.message}")
                    }
                } finally {
                    // 清理资源
                    try {
                        statement?.close()
                        MySQLConnections.closeConnection(connection)
                    } catch (e: SQLException) {
                        Log.e(TAG, "关闭数据库资源失败", e)
                    }
                }
                
                // 每隔两秒执行一次
                try {
                    sleep(2000)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "读取线程被中断")
                    break
                }
            }
        }
        
        /**
         * 安全停止线程
         */
        fun safeStop() {
            isRunning = false
            interrupt()
        }
    }
    
    /**
     * 测试数据库连接线程
     */
    class TestConnectionThread(
        private val activity: AppCompatActivity,
        private val resultCallback: (Boolean) -> Unit
    ) : Thread() {
        
        override fun run() {
            try {
                val isConnected = MySQLConnections.testConnection()
                
                activity.runOnUiThread {
                    if (isConnected) {
                        Toast.makeText(activity, "数据库连接测试成功", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, "数据库连接测试失败", Toast.LENGTH_SHORT).show()
                    }
                    resultCallback(isConnected)
                }
            } catch (e: Exception) {
                Log.e(TAG, "测试数据库连接时发生异常", e)
                
                activity.runOnUiThread {
                    Toast.makeText(activity, "连接测试异常", Toast.LENGTH_SHORT).show()
                    resultCallback(false)
                }
            }
        }
    }
}