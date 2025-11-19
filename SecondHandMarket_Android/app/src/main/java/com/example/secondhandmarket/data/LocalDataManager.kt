package com.example.secondhandmarket.data

import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.data.model.ItemCondition
import com.example.secondhandmarket.data.model.ItemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * 本地数据管理器 - 用于在MySQL数据库连接失败时提供本地模拟数据
 */
object LocalDataManager {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    // 模拟商品数据
    private val mockItems = listOf(
        // 电子产品
        Item(
            id = 1L,
            title = "iPhone 13 Pro 256GB",
            description = "几乎全新的iPhone 13 Pro，深空黑色，256GB存储，电池健康度98%",
            price = 6999.0,
            imageUrl = "https://example.com/iphone13pro.jpg",
            category = ItemCategory.ELECTRONICS,
            location = "北京市朝阳区",
            sellerName = "数码达人小王",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.LIKE_NEW,
            brand = "Apple",
            views = 892,
            likes = 156
        ),
        Item(
            id = 2L,
            title = "MacBook Air M1",
            description = "2020款MacBook Air，M1芯片，8GB内存，256GB SSD，性能强劲",
            price = 5599.0,
            imageUrl = "https://example.com/macbook_air.jpg",
            category = ItemCategory.ELECTRONICS,
            location = "上海市浦东新区",
            sellerName = "程序员小李",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.GOOD,
            brand = "Apple",
            views = 1245,
            likes = 234
        ),
        
        // 服装鞋帽
        Item(
            id = 3L,
            title = "Nike Air Jordan 1 芝加哥",
            description = "正品AJ1芝加哥配色，尺码42，仅穿过2次，品相很好",
            price = 1299.0,
            imageUrl = "https://example.com/aj1_chicago.jpg",
            category = ItemCategory.CLOTHING,
            location = "广州市天河区",
            sellerName = "潮流玩家",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.LIKE_NEW,
            brand = "Nike",
            views = 567,
            likes = 89
        ),
        Item(
            id = 4L,
            title = "优衣库羊毛大衣",
            description = "优衣库经典款羊毛大衣，M码，深蓝色，保暖效果好",
            price = 299.0,
            imageUrl = "https://example.com/wool_coat.jpg",
            category = ItemCategory.CLOTHING,
            location = "深圳市南山区",
            sellerName = "时尚达人小美",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.GOOD,
            brand = "优衣库",
            views = 234,
            likes = 45
        ),
        
        // 图书文具
        Item(
            id = 5L,
            title = "《Java核心技术 卷I》",
            description = "Java编程经典教材，第11版，几乎全新，只看过几次",
            price = 89.0,
            imageUrl = "https://example.com/java_book.jpg",
            category = ItemCategory.BOOKS,
            location = "杭州市西湖区",
            sellerName = "技术书籍爱好者",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.LIKE_NEW,
            brand = "机械工业出版社",
            views = 345,
            likes = 67
        ),
        
        // 家具家电
        Item(
            id = 6L,
            title = "小米空气净化器4 Pro",
            description = "小米空气净化器4 Pro，功能正常，滤芯还有80%使用寿命",
            price = 899.0,
            imageUrl = "https://example.com/air_purifier.jpg",
            category = ItemCategory.FURNITURE,
            location = "成都市高新区",
            sellerName = "家居生活家",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.GOOD,
            brand = "小米",
            views = 678,
            likes = 123
        ),
        
        // 运动户外
        Item(
            id = 7L,
            title = "跑步机家用静音",
            description = "家用静音跑步机，可折叠，不占用空间，适合日常锻炼",
            price = 1299.0,
            imageUrl = "https://example.com/treadmill.jpg",
            category = ItemCategory.SPORTS,
            location = "武汉市洪山区",
            sellerName = "健身爱好者",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.FAIR,
            brand = "Keep",
            views = 456,
            likes = 78
        ),
        
        // 玩具游戏
        Item(
            id = 8L,
            title = "Nintendo Switch OLED",
            description = "任天堂Switch OLED白色款，99新，所有配件齐全",
            price = 2299.0,
            imageUrl = "https://example.com/switch_oled.jpg",
            category = ItemCategory.TOYS,
            location = "西安市雁塔区",
            sellerName = "游戏达人",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.LIKE_NEW,
            brand = "Nintendo",
            views = 1089,
            likes = 267
        ),
        
        // 美妆个护
        Item(
            id = 9L,
            title = "兰蔻小黑瓶精华液",
            description = "兰蔻小黑瓶肌底液，50ml，剩80%，正品保证",
            price = 499.0,
            imageUrl = "https://example.com/lancome_serum.jpg",
            category = ItemCategory.BEAUTY,
            location = "南京市玄武区",
            sellerName = "美妆博主",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.GOOD,
            brand = "兰蔻",
            views = 567,
            likes = 134
        ),
        
        // 数码配件
        Item(
            id = 10L,
            title = "AirPods Pro 2代",
            description = "苹果AirPods Pro 2代，降噪功能正常，充电盒99新",
            price = 1299.0,
            imageUrl = "https://example.com/airpods_pro.jpg",
            category = ItemCategory.DIGITAL,
            location = "重庆市渝中区",
            sellerName = "苹果粉丝",
            createdAt = dateFormat.format(Date()),
            status = ItemStatus.AVAILABLE,
            condition = ItemCondition.LIKE_NEW,
            brand = "Apple",
            views = 789,
            likes = 189
        )
    )
    
    /**
     * 获取所有模拟商品
     */
    suspend fun getAllMockItems(): List<Item> = withContext(Dispatchers.IO) {
        mockItems
    }
    
    /**
     * 根据分类获取模拟商品
     */
    suspend fun getMockItemsByCategory(category: ItemCategory): List<Item> = withContext(Dispatchers.IO) {
        mockItems.filter { it.category == category }
    }
    
    /**
     * 搜索模拟商品
     */
    suspend fun searchMockItems(keyword: String): List<Item> = withContext(Dispatchers.IO) {
        val lowerKeyword = keyword.lowercase()
        mockItems.filter { 
            it.title.lowercase().contains(lowerKeyword) ||
            it.description.lowercase().contains(lowerKeyword) ||
            it.brand.lowercase().contains(lowerKeyword)
        }
    }
    
    /**
     * 获取推荐商品（简单模拟：返回前5个）
     */
    suspend fun getRecommendedMockItems(): List<Item> = withContext(Dispatchers.IO) {
        mockItems.take(5)
    }
    
    /**
     * 获取所有分类
     */
    fun getAllCategories(): List<ItemCategory> {
        return ItemCategory.values().toList()
    }
}