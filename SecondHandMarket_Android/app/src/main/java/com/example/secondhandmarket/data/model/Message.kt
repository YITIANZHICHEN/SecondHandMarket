package com.example.secondhandmarket.data.model

data class Message(
    val id: Long,
    val senderName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val senderAvatar: String = ""
)