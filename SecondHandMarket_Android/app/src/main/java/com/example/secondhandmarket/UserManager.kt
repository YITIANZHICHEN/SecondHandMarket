package com.example.secondhandmarket

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.secondhandmarket.data.model.User

/**
 * 用户数据管理器
 * 负责用户数据的持久化存储和读取
 */
class UserManager private constructor(context: Context) {
    
    companion object {
        @Volatile
        private var instance: UserManager? = null
        
        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences
    private val gson: Gson
    
    init {
        sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        gson = Gson()
    }
    
    /**
     * 保存当前用户
     */
    fun saveCurrentUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString("current_user", userJson)
            .apply()
    }
    
    /**
     * 获取当前用户
     */
    fun getCurrentUser(): User? {
        val userJson = sharedPreferences.getString("current_user", null)
        return if (userJson != null) {
            try {
                gson.fromJson(userJson, User::class.java)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
    
    /**
     * 清除当前用户数据
     */
    fun clearCurrentUser() {
        sharedPreferences.edit()
            .remove("current_user")
            .apply()
    }
    
    /**
     * 检查用户是否已登录
     */
    fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }
    
    /**
     * 保存用户登录状态
     */
    fun saveLoginStatus(loggedIn: Boolean) {
        sharedPreferences.edit()
            .putBoolean("is_logged_in", loggedIn)
            .apply()
    }
    
    /**
     * 获取用户登录状态
     */
    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
    
    /**
     * 保存用户偏好设置
     */
    fun saveUserPreference(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * 获取用户偏好设置
     */
    fun getUserPreference(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 保存用户头像路径
     */
    fun saveUserAvatarPath(userId: String, avatarPath: String) {
        sharedPreferences.edit()
            .putString("avatar_$userId", avatarPath)
            .apply()
    }
    
    /**
     * 获取用户头像路径
     */
    fun getUserAvatarPath(userId: String): String? {
        return sharedPreferences.getString("avatar_$userId", null)
    }
    
    /**
     * 保存用户最近搜索记录
     */
    fun saveSearchHistory(searchTerm: String) {
        val currentHistory = getSearchHistory().toMutableList()
        
        // 移除重复项
        currentHistory.remove(searchTerm)
        
        // 添加到开头
        currentHistory.add(0, searchTerm)
        
        // 限制历史记录数量
        val limitedHistory = if (currentHistory.size > 10) {
            currentHistory.subList(0, 10)
        } else {
            currentHistory
        }
        
        val historyJson = gson.toJson(limitedHistory)
        sharedPreferences.edit()
            .putString("search_history", historyJson)
            .apply()
    }
    
    /**
     * 获取用户搜索历史
     */
    fun getSearchHistory(): List<String> {
        val historyJson = sharedPreferences.getString("search_history", null)
        return if (historyJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(historyJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * 清除搜索历史
     */
    fun clearSearchHistory() {
        sharedPreferences.edit()
            .remove("search_history")
            .apply()
    }
    
    /**
     * 保存用户收藏的商品ID列表
     */
    fun saveFavoriteItems(userId: String, itemIds: List<String>) {
        val favoritesJson = gson.toJson(itemIds)
        sharedPreferences.edit()
            .putString("favorites_$userId", favoritesJson)
            .apply()
    }
    
    /**
     * 获取用户收藏的商品ID列表
     */
    fun getFavoriteItems(userId: String): List<String> {
        val favoritesJson = sharedPreferences.getString("favorites_$userId", null)
        return if (favoritesJson != null) {
            try {
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson<List<String>>(favoritesJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    /**
     * 添加商品到收藏
     */
    fun addToFavorites(userId: String, itemId: String) {
        val currentFavorites = getFavoriteItems(userId).toMutableList()
        if (!currentFavorites.contains(itemId)) {
            currentFavorites.add(itemId)
            saveFavoriteItems(userId, currentFavorites)
        }
    }
    
    /**
     * 从收藏中移除商品
     */
    fun removeFromFavorites(userId: String, itemId: String) {
        val currentFavorites = getFavoriteItems(userId).toMutableList()
        currentFavorites.remove(itemId)
        saveFavoriteItems(userId, currentFavorites)
    }
    
    /**
     * 检查商品是否已收藏
     */
    fun isItemFavorite(userId: String, itemId: String): Boolean {
        return getFavoriteItems(userId).contains(itemId)
    }
    
    /**
     * 保存应用首次启动状态
     */
    fun setFirstLaunch(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean("first_launch_completed", completed)
            .apply()
    }
    
    /**
     * 检查是否是首次启动
     */
    fun isFirstLaunch(): Boolean {
        return !sharedPreferences.getBoolean("first_launch_completed", false)
    }
    
    /**
     * 清除所有用户数据（注销时使用）
     */
    fun clearAllUserData() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}