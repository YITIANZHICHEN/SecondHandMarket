package com.example.secondhandmarket.data

import android.content.Context
import android.util.Log
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.database.ItemDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

object ItemDataGenerator {
    
    private val itemDao = ItemDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var currentMode = "mysql" // "mysql" 或 "local"
    private var isInitialized = false
    
    /**
     * 生成模拟商品数据
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 商品列表
     */
    suspend fun generateMockItems(page: Int = 1, pageSize: Int = 10): List<Item> {
        Log.d("ItemDataGenerator", "generateMockItems 被调用: page=$page, pageSize=$pageSize, currentMode=$currentMode, isInitialized=${isInitialized()}")
        
        return try {
            // 如果数据库已初始化且处于数据库模式，从数据库获取数据
            if (currentMode == "mysql" && isInitialized()) {
                Log.d("ItemDataGenerator", "尝试从数据库模式获取数据")
                getItemsFromDatabase(page, pageSize)
            } else {
                Log.d("ItemDataGenerator", "尝试从本地模式获取数据")
                getItemsFromLocal(page, pageSize)
            }
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "generateMockItems 获取数据时发生错误", e)
            // 发生错误时尝试从本地获取数据作为备用
            Log.d("ItemDataGenerator", "错误时尝试从本地获取备用数据")
            try {
                getItemsFromLocal(page, pageSize)
            } catch (fallbackException: Exception) {
                Log.e("ItemDataGenerator", "获取备用本地数据也失败", fallbackException)
                emptyList()
            }
        }
    }
    
    /**
     * 从数据库获取商品列表
     */
    private suspend fun getItemsFromDatabase(page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从数据库获取商品列表: page $page pageSize $pageSize")
            
            // 首先尝试从数据库获取
            val itemsFromDb = itemDao.getAllItems(page, pageSize)
            
            if (itemsFromDb.isNotEmpty()) {
                Log.d("ItemDataGenerator", "从数据库成功获取 ${itemsFromDb.size} 个商品")
                itemsFromDb
            } else {
                Log.d("ItemDataGenerator", "数据库暂无数据，尝试初始化测试数据")
                
                // 如果数据库为空，尝试初始化测试数据
                val initialized = itemDao.initializeTestData()
                if (initialized) {
                    // 重新获取测试数据
                    itemDao.getAllItems(page, pageSize).also { items ->
                        Log.d("ItemDataGenerator", "初始化测试数据后，获取到 ${items.size} 个商品")
                        return items
                    }
                } else {
                    Log.e("ItemDataGenerator", "初始化测试数据失败，切换到本地模式")
                    switchToLocalMode()
                    getItemsFromLocal(page, pageSize)
                }
            }
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从数据库获取商品列表失败，切换到本地模式", e)
            switchToLocalMode()
            getItemsFromLocal(page, pageSize)
        }
    }
    
    /**
     * 从本地数据源获取商品列表
     */
    private suspend fun getItemsFromLocal(page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从本地数据源获取商品列表: page $page pageSize $pageSize")
            
            val allItems = LocalDataManager.getAllMockItems()
            
            // 计算分页
            val startIndex = (page - 1) * pageSize
            val endIndex = startIndex + pageSize
            
            if (startIndex >= allItems.size) {
                Log.d("ItemDataGenerator", "本地数据源：页码超出范围，返回空列表")
                emptyList()
            } else {
                val pagedItems = allItems.subList(startIndex, minOf(endIndex, allItems.size))
                Log.d("ItemDataGenerator", "本地数据源返回 ${pagedItems.size} 个商品 (第 $page 页)")
                pagedItems
            }
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从本地数据源获取商品列表失败", e)
            emptyList()
        }
    }
    
    /**
     * 根据分类获取商品（智能模式）
     */
    suspend fun getItemsByCategory(category: ItemCategory, page: Int = 1, pageSize: Int = 10): List<Item> {
        return if (currentMode == "local" || !isInitialized()) {
            getItemsByCategoryFromLocal(category, page, pageSize)
        } else {
            getItemsByCategoryFromDatabase(category, page, pageSize)
        }
    }
    
    /**
     * 从数据库根据分类获取商品
     */
    private suspend fun getItemsByCategoryFromDatabase(category: ItemCategory, page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从数据库获取 ${category.name} 分类商品: page $page pageSize $pageSize")
            
            val itemsFromDb = itemDao.getItemsByCategory(category, page, pageSize)
            
            if (itemsFromDb.isNotEmpty()) {
                Log.d("ItemDataGenerator", "从数据库成功获取 ${itemsFromDb.size} 个 ${category.name} 商品")
                itemsFromDb
            } else {
                Log.d("ItemDataGenerator", "数据库暂无 ${category.name} 分类商品，尝试初始化测试数据")
                
                // 如果数据库为空，尝试初始化测试数据
                val initialized = itemDao.initializeTestData()
                if (initialized) {
                    // 重新获取分类数据
                    itemDao.getItemsByCategory(category, page, pageSize).also { items ->
                        Log.d("ItemDataGenerator", "初始化测试数据后，获取到 ${items.size} 个 ${category.name} 商品")
                        return items
                    }
                } else {
                    Log.e("ItemDataGenerator", "初始化测试数据失败，切换到本地模式")
                    switchToLocalMode()
                    getItemsByCategoryFromLocal(category, page, pageSize)
                }
            }
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从数据库获取 ${category.name} 分类商品失败，切换到本地模式", e)
            switchToLocalMode()
            getItemsByCategoryFromLocal(category, page, pageSize)
        }
    }
    
    /**
     * 从本地数据源根据分类获取商品
     */
    private suspend fun getItemsByCategoryFromLocal(category: ItemCategory, page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从本地数据源获取 ${category.name} 分类商品: page $page pageSize $pageSize")
            
            val allCategoryItems = LocalDataManager.getMockItemsByCategory(category)
            
            // 计算分页
            val startIndex = (page - 1) * pageSize
            val endIndex = startIndex + pageSize
            
            if (startIndex >= allCategoryItems.size) {
                Log.d("ItemDataGenerator", "本地数据源分类 ${category.name}：页码超出范围，返回空列表")
                emptyList()
            } else {
                val pagedItems = allCategoryItems.subList(startIndex, minOf(endIndex, allCategoryItems.size))
                Log.d("ItemDataGenerator", "本地数据源分类 ${category.name} 返回 ${pagedItems.size} 个商品 (第 $page 页)")
                pagedItems
            }
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从本地数据源获取 ${category.name} 分类商品失败", e)
            emptyList()
        }
    }
    
    /**
     * 搜索商品（智能模式）
     */
    suspend fun searchItems(keyword: String, page: Int = 1, pageSize: Int = 10): List<Item> {
        return if (currentMode == "local" || !isInitialized()) {
            searchItemsFromLocal(keyword, page, pageSize)
        } else {
            searchItemsFromDatabase(keyword, page, pageSize)
        }
    }
    
    /**
     * 从数据库搜索商品
     */
    private suspend fun searchItemsFromDatabase(keyword: String, page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从数据库搜索关键词 '$keyword': page $page pageSize $pageSize")
            
            val itemsFromDb = itemDao.searchItems(keyword, page, pageSize)
            
            if (itemsFromDb.isNotEmpty()) {
                Log.d("ItemDataGenerator", "从数据库搜索到 ${itemsFromDb.size} 个匹配的商品")
                itemsFromDb
            } else {
                Log.d("ItemDataGenerator", "数据库暂无匹配 '$keyword' 的商品，尝试初始化测试数据")
                
                // 如果数据库为空，尝试初始化测试数据
                val initialized = itemDao.initializeTestData()
                if (initialized) {
                    // 重新搜索
                    itemDao.searchItems(keyword, page, pageSize).also { items ->
                        Log.d("ItemDataGenerator", "初始化测试数据后，搜索到 ${items.size} 个匹配的商品")
                        return items
                    }
                } else {
                    Log.e("ItemDataGenerator", "初始化测试数据失败，切换到本地模式")
                    switchToLocalMode()
                    searchItemsFromLocal(keyword, page, pageSize)
                }
            }
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从数据库搜索关键词 '$keyword' 的商品失败，切换到本地模式", e)
            switchToLocalMode()
            searchItemsFromLocal(keyword, page, pageSize)
        }
    }
    
    /**
     * 从本地数据源搜索商品
     */
    private suspend fun searchItemsFromLocal(keyword: String, page: Int = 1, pageSize: Int = 10): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从本地数据源搜索关键词 '$keyword': page $page pageSize $pageSize")
            LocalDataManager.searchMockItems(keyword)
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从本地数据源搜索关键词 '$keyword' 的商品失败", e)
            emptyList()
        }
    }
    
    /**
     * 获取所有分类
     * 这个方法保持不变，因为分类是固定的枚举
     */
    fun getAllCategories(): List<ItemCategory> {
        return ItemCategory.values().toList()
    }
    
    /**
     * 获取分类标题映射
     * 这个方法保持不变，因为分类标题是固定的
     */
    fun getCategoryTitleMap(): Map<ItemCategory, String> {
        return mapOf(
            ItemCategory.ELECTRONICS to "电子产品",
            ItemCategory.CLOTHING to "服装鞋帽",
            ItemCategory.BOOKS to "图书文具",
            ItemCategory.FURNITURE to "家具家电",
            ItemCategory.SPORTS to "运动户外",
            ItemCategory.TOYS to "玩具游戏",
            ItemCategory.BEAUTY to "美妆个护",
            ItemCategory.DIGITAL to "数码配件",
            ItemCategory.HOME to "家居用品",
            ItemCategory.OTHER to "其他"
        )
    }
    
    /**
     * 获取分类图标映射
     * 这个方法保持不变，因为分类图标资源是固定的
     */
    fun getCategoryIconMap(): Map<ItemCategory, String> {
        return mapOf(
            ItemCategory.ELECTRONICS to "ic_electronics",
            ItemCategory.CLOTHING to "ic_clothing",
            ItemCategory.BOOKS to "ic_books",
            ItemCategory.FURNITURE to "ic_furniture",
            ItemCategory.SPORTS to "ic_sports",
            ItemCategory.TOYS to "ic_toys",
            ItemCategory.BEAUTY to "ic_beauty",
            ItemCategory.DIGITAL to "ic_digital",
            ItemCategory.HOME to "ic_home",
            ItemCategory.OTHER to "ic_other"
        )
    }
    
    /**
     * 获取推荐商品（智能模式）
     */
    suspend fun getRecommendedItems(limit: Int = 5): List<Item> {
        return if (currentMode == "local" || !isInitialized()) {
            getRecommendedItemsFromLocal(limit)
        } else {
            getRecommendedItemsFromDatabase(limit)
        }
    }
    
    /**
     * 从数据库获取推荐商品
     */
    private suspend fun getRecommendedItemsFromDatabase(limit: Int = 5): List<Item> {
        return try {
            val allItems = itemDao.getAllItems(1, limit * 2) // 获取更多以便筛选
            allItems.sortedByDescending { it.likes }
                .take(limit)
                .also { items ->
                    Log.d("ItemDataGenerator", "从数据库获取到 ${items.size} 个推荐商品")
                }
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从数据库获取推荐商品失败，切换到本地模式", e)
            switchToLocalMode()
            getRecommendedItemsFromLocal(limit)
        }
    }
    
    /**
     * 从本地数据源获取推荐商品
     */
    private suspend fun getRecommendedItemsFromLocal(limit: Int = 5): List<Item> {
        return try {
            Log.d("ItemDataGenerator", "从本地数据源获取推荐商品: limit $limit")
            val allRecommendedItems = LocalDataManager.getRecommendedMockItems()
            val result = allRecommendedItems.take(limit)
            Log.d("ItemDataGenerator", "本地数据源返回 ${result.size} 个推荐商品")
            result
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "从本地数据源获取推荐商品失败", e)
            emptyList()
        }
    }
    
    /**
     * 初始化数据库连接和测试数据
     * 智能模式：检查FORCE_MOCK_MODE配置，如果为true则直接使用本地模式
     */
    suspend fun initializeDatabase(context: Context) {
        try {
            Log.d("ItemDataGenerator", "正在初始化数据源...")
            
            // 检查FORCE_MOCK_MODE配置
            val forceMockMode = readForceMockModeFromConfig(context)
            
            if (forceMockMode) {
                Log.d("ItemDataGenerator", "检测到FORCE_MOCK_MODE=true，强制使用本地模拟数据模式")
                switchToLocalMode()
                initializeLocalDatabase()
                return
            }
            
            // 尝试初始化MySQL数据库
            val mysqlInitialized = initializeMySQLDatabase()
            
            if (mysqlInitialized) {
                Log.d("ItemDataGenerator", "MySQL数据库初始化成功，使用数据库模式")
                currentMode = "mysql"
                isInitialized = true
            } else {
                Log.d("ItemDataGenerator", "MySQL初始化失败，切换到本地模式")
                switchToLocalMode()
                initializeLocalDatabase()
            }
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "初始化数据源时发生异常，切换到本地模式", e)
            switchToLocalMode()
            initializeLocalDatabase()
        }
    }
    
    /**
     * 初始化MySQL数据库
     */
    private suspend fun initializeMySQLDatabase(): Boolean {
        return try {
            Log.d("ItemDataGenerator", "正在初始化MySQL数据库...")
            
            // 获取DatabaseManager实例并初始化配置
            val databaseManager = com.example.secondhandmarket.database.DatabaseManager.getInstance()
            databaseManager.initializeConfig()
            
            // 初始化数据库连接
            val connectionInitialized = databaseManager.initialize()
            
            if (connectionInitialized) {
                // 尝试初始化测试数据
                val initialized = itemDao.initializeTestData()
                
                if (initialized) {
                    Log.d("ItemDataGenerator", "MySQL数据库初始化成功")
                    true
                } else {
                    Log.e("ItemDataGenerator", "MySQL数据库初始化失败")
                    false
                }
            } else {
                Log.e("ItemDataGenerator", "MySQL数据库连接初始化失败")
                false
            }
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "初始化MySQL数据库时发生异常", e)
            false
        }
    }
    
    /**
     * 初始化本地数据库
     */
    private suspend fun initializeLocalDatabase(): Boolean {
        return try {
            Log.d("ItemDataGenerator", "正在初始化本地数据源...")
            
            // LocalDataManager是静态对象，无需初始化，直接设置状态
            Log.d("ItemDataGenerator", "本地数据源初始化成功")
            isInitialized = true
            true
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "初始化本地数据源时发生异常", e)
            false
        }
    }
    
    /**
     * 从app_config.properties读取FORCE_MOCK_MODE配置
     */
    private fun readForceMockModeFromConfig(context: Context): Boolean {
        return try {
            val inputStream = context.assets.open("app_config.properties")
            val properties = java.util.Properties()
            properties.load(inputStream)
            inputStream.close()
            
            val forceMockModeStr = properties.getProperty("FORCE_MOCK_MODE", "true")
            val forceMockMode = forceMockModeStr.toBoolean()
            
            Log.d("ItemDataGenerator", "读取到FORCE_MOCK_MODE配置: $forceMockMode")
            forceMockMode
            
        } catch (e: Exception) {
            Log.e("ItemDataGenerator", "读取app_config.properties失败，默认使用模拟数据模式", e)
            true // 默认使用模拟数据模式
        }
    }
    
    /**
     * 切换到本地模式
     */
    private suspend fun switchToLocalMode() {
        if (currentMode != "local") {
            Log.d("ItemDataGenerator", "切换到本地模式")
            currentMode = "local"
            // 这里需要在合适的时候初始化本地数据
        }
    }
    
    /**
     * 获取当前数据源模式
     */
    fun getCurrentMode(): String {
        return currentMode
    }
    
    /**
     * 检查数据源是否已初始化
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }
    
    /**
     * 在指定协程中异步初始化数据库
     */
    fun initializeDatabaseAsync(scope: CoroutineScope, context: Context) {
        scope.launch {
            initializeDatabase(context)
        }
    }
}