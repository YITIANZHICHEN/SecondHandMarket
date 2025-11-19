package com.example.secondhandmarket.data.model

import java.io.Serializable

data class User(
    val username: String,
    val password: String,
    val email: String? = null,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val gender: String? = null,
    val birthday: String? = null
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
        
        // 默认用户头像
        const val DEFAULT_AVATAR = "@android:drawable/ic_menu_gallery"
    }
    
    // 获取显示名称（优先使用昵称，没有则使用用户名）
    fun getDisplayName(): String {
        return nickname ?: username
    }
    
    // 获取头像URL（如果没有设置则返回默认头像）
    fun getAvatar(): String {
        return avatarUrl ?: DEFAULT_AVATAR
    }
}