package com.example.secondhandmarket.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: Long,
    val title: String,
    val price: Double,
    val imageUrl: String,
    val description: String,
    val category: ItemCategory,
    val location: String,
    val sellerId: Long = 0,
    val sellerName: String = "",
    val createdAt: String = "",
    val status: ItemStatus = ItemStatus.AVAILABLE,
    val condition: ItemCondition = ItemCondition.GOOD,
    val brand: String = "",
    val views: Int = 0,
    val likes: Int = 0,
    var isFavorite: Boolean = false
) : Parcelable

enum class ItemStatus {
    AVAILABLE, // 可购买
    SOLD,      // 已售出
    RESERVED   // 已预订
}

enum class ItemCategory {
    ELECTRONICS,    // 电子产品
    CLOTHING,       // 服装鞋帽
    BOOKS,          // 图书文具
    HOME,           // 家居用品
    SPORTS,         // 运动户外
    BEAUTY,         // 美妆个护
    TOYS,           // 玩具游戏
    DIGITAL,        // 数码配件
    FURNITURE,      // 家具家电
    OTHER           // 其他
}

enum class ItemCondition {
    NEW,            // 全新
    LIKE_NEW,       // 几乎全新
    GOOD,           // 良好
    FAIR,           // 一般
    POOR            // 较差
}