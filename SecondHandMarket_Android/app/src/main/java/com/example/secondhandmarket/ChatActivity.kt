package com.example.secondhandmarket

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandmarket.data.model.Item

import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    
    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private lateinit var toolbar: Toolbar
    private lateinit var messageAdapter: MessageAdapter
    private var sellerName: String = ""
    private var currentItem: Item? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // 获取传递的数据
        sellerName = intent.getStringExtra("seller_name") ?: "卖家"
        currentItem = intent.getParcelableExtra("item_data")
        
        initViews()
        setupToolbar()
        setupRecyclerView()
        setupSendButton()
        addWelcomeMessage()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewMessages = findViewById(R.id.recycler_view_messages)
        editTextMessage = findViewById(R.id.edit_text_message)
        buttonSend = findViewById(R.id.button_send)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "与 $sellerName 聊天"
        
        // 如果有商品信息，显示商品标题
        currentItem?.let { item ->
            supportActionBar?.subtitle = "关于: ${item.title}"
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        recyclerViewMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerViewMessages.adapter = messageAdapter
    }
    
    private fun setupSendButton() {
        buttonSend.setOnClickListener {
            val messageText = editTextMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                editTextMessage.text.clear()
            } else {
                Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = Message(
            id = "welcome_${System.currentTimeMillis()}",
            text = "你好！我对${currentItem?.title ?: "这件商品"}很感兴趣，可以详细了解一下吗？",
            isFromMe = true,
            timestamp = System.currentTimeMillis()
        )
        messageAdapter.addMessage(welcomeMessage)
    }
    
    private fun sendMessage(text: String) {
        val message = Message(
            id = "msg_${System.currentTimeMillis()}",
            text = text,
            isFromMe = true,
            timestamp = System.currentTimeMillis()
        )
        messageAdapter.addMessage(message)
        
        // 模拟卖家回复
        simulateSellerReply()
    }
    
    private fun simulateSellerReply() {
        recyclerViewMessages.postDelayed({
            val replies = listOf(
                "您好！很高兴您对这件商品感兴趣",
                "这件商品保养得很好，几乎没有使用痕迹",
                "价格方面我们还可以再商议一下",
                "如果您方便的话，我们可以约个时间当面看货",
                "谢谢您的咨询，有什么问题随时联系我",
                "商品是正品，有购买凭证可以提供",
                "我可以给您拍更多实物照片",
                "期待与您的交易！"
            )
            
            val randomReply = replies.random()
            val replyMessage = Message(
                id = "reply_${System.currentTimeMillis()}",
                text = randomReply,
                isFromMe = false,
                timestamp = System.currentTimeMillis()
            )
            messageAdapter.addMessage(replyMessage)
            
            Toast.makeText(this, "卖家回复了", Toast.LENGTH_SHORT).show()
        }, 1000 + (Math.random() * 2000).toLong()) // 1-3秒后回复
    }
    
    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(menuItem)
        }
    }
    
    // 聊天消息数据类
    data class Message(
        val id: String,
        val text: String,
        val isFromMe: Boolean,
        val timestamp: Long
    )
    
    // 消息适配器
    class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
        
        private val messages = mutableListOf<Message>()
        
        fun addMessage(message: Message) {
            messages.add(message)
            notifyItemInserted(messages.size - 1)
        }
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): MessageViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(if (viewType == R.layout.item_message_sent) R.layout.item_message_sent else R.layout.item_message_received, parent, false)
            return MessageViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.bind(messages[position])
        }
        
        override fun getItemCount(): Int = messages.size
        
        override fun getItemViewType(position: Int): Int {
            return if (messages[position].isFromMe) {
                R.layout.item_message_sent
            } else {
                R.layout.item_message_received
            }
        }
        
        class MessageViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
            private val textViewMessage: TextView = itemView.findViewById(R.id.text_view_message)
            private val textViewTime: TextView = itemView.findViewById(R.id.text_view_time)
            
            fun bind(message: Message) {
                textViewMessage.text = message.text
                textViewTime.text = formatTime(message.timestamp)
            }
            
            private fun formatTime(timestamp: Long): String {
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                return sdf.format(Date(timestamp))
            }
        }
    }
}