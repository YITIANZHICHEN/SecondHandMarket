package com.example.secondhandmarket.data

import android.content.Context
import android.content.SharedPreferences
import com.example.secondhandmarket.data.model.Item

/**
 * 收藏管理器
 * 负责管理用户收藏的商品数据，使用SharedPreferences本地存储
 */
class FavoriteManager(private val context: Context) {
    
    companion object {
        private const val FAVORITE_PREFS = "favorite_items"
        private const val KEY_FAVORITES = "favorite_item_ids"
        private const val PREF_NAME = "SecondHandMarket"
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * 获取所有收藏的商品ID列表
     */
    private fun getFavoriteItemIds(): Set<String> {
        return sharedPreferences.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }
    
    /**
     * 保存收藏的商品ID列表
     */
    private fun saveFavoriteItemIds(itemIds: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_FAVORITES, itemIds).apply()
    }
    
    /**
     * 检查商品是否被收藏
     */
    fun isItemFavorite(itemId: Long): Boolean {
        return getFavoriteItemIds().contains(itemId.toString())
    }
    
    /**
     * 收藏商品
     */
    fun addToFavorites(item: Item): Boolean {
        return try {
            val favoriteIds = getFavoriteItemIds().toMutableSet()
            favoriteIds.add(item.id.toString())
            saveFavoriteItemIds(favoriteIds)
            
            // 更新Item的isFavorite状态
            item.isFavorite = true
            
            android.util.Log.d("FavoriteManager", "已收藏商品: ${item.title} (ID: ${item.id})")
            true
        } catch (e: Exception) {
            android.util.Log.e("FavoriteManager", "收藏商品失败", e)
            false
        }
    }
    
    /**
     * 取消收藏商品
     */
    fun removeFromFavorites(itemId: Long): Boolean {
        return try {
            val favoriteIds = getFavoriteItemIds().toMutableSet()
            favoriteIds.remove(itemId.toString())
            saveFavoriteItemIds(favoriteIds)
            
            android.util.Log.d("FavoriteManager", "已取消收藏商品 ID: $itemId")
            true
        } catch (e: Exception) {
            android.util.Log.e("FavoriteManager", "取消收藏商品失败", e)
            false
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite(item: Item): Boolean {
        return if (isItemFavorite(item.id)) {
            removeFromFavorites(item.id)
        } else {
            addToFavorites(item)
        }
    }
    
    /**
     * 获取收藏商品总数
     */
    fun getFavoriteCount(): Int {
        return getFavoriteItemIds().size
    }
    
    /**
     * 清除所有收藏
     */
    fun clearAllFavorites(): Boolean {
        return try {
            saveFavoriteItemIds(emptySet())
            android.util.Log.d("FavoriteManager", "已清除所有收藏")
            true
        } catch (e: Exception) {
            android.util.Log.e("FavoriteManager", "清除收藏失败", e)
            false
        }
    }
    
    /**
     * 获取所有收藏的商品ID列表
     */
    fun getAllFavoriteIds(): List<Long> {
        return getFavoriteItemIds()
            .mapNotNull { it.toLongOrNull() }
            .sorted()
    }
    
    /**
     * 根据收藏状态更新Item列表
     * @param items Item列表
     * @return 更新后的Item列表
     */
    fun updateFavoriteStatus(items: List<Item>): List<Item> {
        val favoriteIds = getFavoriteItemIds()
        return items.map { item ->
            item.copy(isFavorite = favoriteIds.contains(item.id.toString()))
        }
    }
}