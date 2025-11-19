package com.example.secondhandmarket.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secondhandmarket.R
import com.example.secondhandmarket.database.DatabaseThreads
import com.example.secondhandmarket.database.MySQLConnections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 数据库测试Activity
 * 演示优化后的数据库连接功能
 * 参考Java实例实现点击按钮发送数据，并每隔两秒获取数据库中数据
 */
class DatabaseTestActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DatabaseTestActivity"
    }
    
    // UI组件
    private lateinit var tvStatus: TextView
    private lateinit var tvData: TextView
    private lateinit var btnSend: Button
    private lateinit var btnTest: Button
    
    // 数据库线程
    private lateinit var sendThread: DatabaseThreads.SendMessageThread
    private lateinit var readThread: DatabaseThreads.ReadDataThread
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_test)
        
        // 初始化UI组件
        initViews()
        
        // 初始化数据库线程
        initDatabaseThreads()
        
        // 启动读取线程
        readThread.start()
        
        // 启动发送线程
        sendThread.start()
    }
    
    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvData = findViewById(R.id.tvData)
        btnSend = findViewById(R.id.btnSend)
        btnTest = findViewById(R.id.btnTest)
        
        // 设置按钮点击监听器
        btnSend.setOnClickListener {
            // 设置发送标志位
            sendThread.setShouldSend(true)
            updateStatus("准备发送消息...")
        }
        
        btnTest.setOnClickListener {
            // 测试数据库连接
            testDatabaseConnection()
        }
        
        // 初始状态
        updateStatus("数据库测试界面已就绪")
        tvData.text = "等待数据加载..."
    }
    
    private fun initDatabaseThreads() {
        // 初始化发送消息线程
        sendThread = DatabaseThreads.SendMessageThread(
            activity = this,
            messageCallback = { message ->
                updateStatus("发送成功: $message")
                Log.d(TAG, "消息发送成功: $message")
            },
            errorCallback = { error ->
                updateStatus("发送失败: $error")
                Log.e(TAG, "消息发送失败: $error")
            }
        )
        
        // 初始化读取数据线程
        readThread = DatabaseThreads.ReadDataThread(
            activity = this,
            dataCallback = { messages ->
                updateDataDisplay(messages)
                Log.d(TAG, "收到 ${messages.size} 条消息")
            },
            errorCallback = { error ->
                updateStatus("读取失败: $error")
                Log.e(TAG, "数据读取失败: $error")
            }
        )
    }
    
    private fun testDatabaseConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateStatus("正在测试数据库连接...")
                
                // 使用MySQLConnections测试连接
                val isConnected = MySQLConnections.testConnection()
                
                runOnUiThread {
                    if (isConnected) {
                        updateStatus("数据库连接测试成功")
                        Toast.makeText(this@DatabaseTestActivity, "数据库连接正常", Toast.LENGTH_SHORT).show()
                    } else {
                        updateStatus("数据库连接测试失败")
                        Toast.makeText(this@DatabaseTestActivity, "数据库连接失败", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "测试数据库连接时发生异常", e)
                runOnUiThread {
                    updateStatus("连接测试异常: ${e.message}")
                    Toast.makeText(this@DatabaseTestActivity, "连接测试异常", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateStatus(status: String) {
        runOnUiThread {
            tvStatus.text = "状态: $status"
        }
    }
    
    private fun updateDataDisplay(messages: List<String>) {
        runOnUiThread {
            if (messages.isNotEmpty()) {
                val displayText = StringBuilder()
                displayText.append("最新消息:\n\n")
                messages.forEachIndexed { index, message ->
                    displayText.append("${index + 1}. $message\n\n")
                }
                tvData.text = displayText.toString()
            } else {
                tvData.text = "暂无数据"
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 安全停止线程
        sendThread.safeStop()
        readThread.safeStop()
        
        Log.d(TAG, "Activity销毁，线程已停止")
    }
    
    /**
     * 快速发送测试消息
     */
    fun sendTestMessage() {
        sendThread.setShouldSend(true)
    }
    
    /**
     * 获取当前状态
     */
    fun getCurrentStatus(): String {
        return tvStatus.text.toString()
    }
}