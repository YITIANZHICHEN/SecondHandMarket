package com.example.secondhandmarket.database

import android.util.Log
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.data.model.ItemCondition
import com.example.secondhandmarket.data.model.ItemStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException

class ItemDao {
    
    companion object {
        private const val TAG = "ItemDao"
    }
    
    private val databaseManager = DatabaseManager.getInstance()
    
    private fun mapResultSetToItem(resultSet: ResultSet): Item {
        return Item(
            id = resultSet.getLong("id"),
            title = resultSet.getString("title"),
            price = resultSet.getDouble("price"),
            imageUrl = resultSet.getString("image_url"),
            description = resultSet.getString("description"),
            category = ItemCategory.valueOf(resultSet.getString("category")),
            location = resultSet.getString("location"),
            sellerId = resultSet.getLong("seller_id"),
            sellerName = resultSet.getString("seller_name"),
            createdAt = resultSet.getString("created_at"),
            status = ItemStatus.valueOf(resultSet.getString("status")),
            condition = ItemCondition.valueOf(resultSet.getString("condition")),
            brand = resultSet.getString("brand"),
            views = resultSet.getInt("views"),
            likes = resultSet.getInt("likes")
        )
    }
    
    suspend fun getAllItems(page: Int = 1, pageSize: Int = 10): List<Item> {
        return withContext(Dispatchers.IO) {
            val items = mutableListOf<Item>()
            
            try {
                val offset = (page - 1) * pageSize
                val sql = "SELECT * FROM items ORDER BY created_at DESC LIMIT ? OFFSET ?"
                
                databaseManager.executeQuery(sql, listOf(pageSize, offset))?.use { resultSet ->
                    while (resultSet.next()) {
                        items.add(mapResultSetToItem(resultSet))
                    }
                }
                
                Log.d(TAG, "Loaded ${items.size} items for page $page")
                
            } catch (e: SQLException) {
                Log.e(TAG, "Failed to get items", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting items", e)
            }
            
            items
        }
    }
    
    suspend fun getItemsByCategory(category: ItemCategory, page: Int = 1, pageSize: Int = 10): List<Item> {
        return withContext(Dispatchers.IO) {
            val items = mutableListOf<Item>()
            
            try {
                val offset = (page - 1) * pageSize
                val sql = "SELECT * FROM items WHERE category = ? ORDER BY created_at DESC LIMIT ? OFFSET ?"
                
                databaseManager.executeQuery(sql, listOf(category.name, pageSize, offset))?.use { resultSet ->
                    while (resultSet.next()) {
                        items.add(mapResultSetToItem(resultSet))
                    }
                }
                
                Log.d(TAG, "Loaded ${items.size} items for category ${category.name}")
                
            } catch (e: SQLException) {
                Log.e(TAG, "Failed to get items by category", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting items by category", e)
            }
            
            items
        }
    }
    
    suspend fun getItemById(id: Long): Item? {
        return withContext(Dispatchers.IO) {
            try {
                val sql = "SELECT * FROM items WHERE id = ?"
                
                databaseManager.executeQuery(sql, listOf(id))?.use { resultSet ->
                    if (resultSet.next()) {
                        val item = mapResultSetToItem(resultSet)
                        Log.d(TAG, "Found item: ${item.title}")
                        return@withContext item
                    }
                }
                
                Log.d(TAG, "Item with id $id not found")
                return@withContext null
                
            } catch (e: SQLException) {
                Log.e(TAG, "Failed to get item by id", e)
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting item by id", e)
                return@withContext null
            }
        }
    }
    
    suspend fun searchItems(keyword: String, page: Int = 1, pageSize: Int = 10): List<Item> {
        return withContext(Dispatchers.IO) {
            val items = mutableListOf<Item>()
            
            try {
                val offset = (page - 1) * pageSize
                val sql = """
                    SELECT * FROM items 
                    WHERE title LIKE ? OR description LIKE ? OR brand LIKE ? 
                    ORDER BY created_at DESC LIMIT ? OFFSET ?
                """.trimIndent()
                
                val searchPattern = "%$keyword%"
                databaseManager.executeQuery(sql, listOf(searchPattern, searchPattern, searchPattern, pageSize, offset))?.use { resultSet ->
                    while (resultSet.next()) {
                        items.add(mapResultSetToItem(resultSet))
                    }
                }
                
                Log.d(TAG, "Search keyword '$keyword' found ${items.size} items")
                
            } catch (e: SQLException) {
                Log.e(TAG, "Failed to search items", e)
            } catch (e: Exception) {
                Log.e(TAG, "Exception when searching items", e)
            }
            
            items
        }
    }
    
    suspend fun insertItem(item: Item): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sql = """
                    INSERT INTO items (
                        title, price, image_url, description, category, location, 
                        seller_id, seller_name, created_at, status, condition, brand, views, likes
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent()
                
                val params = listOf(
                    item.title,
                    item.price,
                    item.imageUrl,
                    item.description,
                    item.category.name,
                    item.location,
                    item.sellerId,
                    item.sellerName,
                    item.createdAt,
                    item.status.name,
                    item.condition.name,
                    item.brand,
                    item.views,
                    item.likes
                )
                
                val affectedRows = databaseManager.executeUpdate(sql, params)
                val success = affectedRows > 0
                
                if (success) {
                    Log.d(TAG, "Successfully inserted item: ${item.title}")
                } else {
                    Log.e(TAG, "Failed to insert item: ${item.title}")
                }
                
                return@withContext success
                
            } catch (e: SQLException) {
                Log.e(TAG, "SQL exception when inserting item", e)
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "Exception when inserting item", e)
                return@withContext false
            }
        }
    }
    
    suspend fun updateItem(item: Item): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sql = """
                    UPDATE items SET 
                        title = ?, price = ?, image_url = ?, description = ?, 
                        category = ?, location = ?, seller_id = ?, seller_name = ?, 
                        created_at = ?, status = ?, condition = ?, brand = ?, views = ?, likes = ?
                    WHERE id = ?
                """.trimIndent()
                
                val params = listOf(
                    item.title,
                    item.price,
                    item.imageUrl,
                    item.description,
                    item.category.name,
                    item.location,
                    item.sellerId,
                    item.sellerName,
                    item.createdAt,
                    item.status.name,
                    item.condition.name,
                    item.brand,
                    item.views,
                    item.likes,
                    item.id
                )
                
                val affectedRows = databaseManager.executeUpdate(sql, params)
                val success = affectedRows > 0
                
                if (success) {
                    Log.d(TAG, "Successfully updated item: ${item.title}")
                } else {
                    Log.e(TAG, "Failed to update item: ${item.title}")
                }
                
                return@withContext success
                
            } catch (e: SQLException) {
                Log.e(TAG, "SQL exception when updating item", e)
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "Exception when updating item", e)
                return@withContext false
            }
        }
    }
    
    suspend fun deleteItem(id: Long): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val sql = "DELETE FROM items WHERE id = ?"
                
                val affectedRows = databaseManager.executeUpdate(sql, listOf(id))
                val success = affectedRows > 0
                
                if (success) {
                    Log.d(TAG, "Successfully deleted item: ID $id")
                } else {
                    Log.e(TAG, "Failed to delete item: ID $id")
                }
                
                return@withContext success
                
            } catch (e: SQLException) {
                Log.e(TAG, "SQL exception when deleting item", e)
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "Exception when deleting item", e)
                return@withContext false
            }
        }
    }
    
    suspend fun getTotalCount(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val sql = "SELECT COUNT(*) as total FROM items"
                
                databaseManager.executeQuery(sql)?.use { resultSet ->
                    if (resultSet.next()) {
                        return@withContext resultSet.getInt("total")
                    }
                }
                
                return@withContext 0
                
            } catch (e: SQLException) {
                Log.e(TAG, "Failed to get total count", e)
                return@withContext 0
            } catch (e: Exception) {
                Log.e(TAG, "Exception when getting total count", e)
                return@withContext 0
            }
        }
    }
    
    suspend fun initializeTestData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val existingCount = getTotalCount()
                if (existingCount > 0) {
                    Log.d(TAG, "Database already has $existingCount items, skipping test data initialization")
                    return@withContext true
                }
                
                Log.d(TAG, "Starting test data initialization...")
                
                val testItems = createTestItems()
                var successCount = 0
                
                testItems.forEach { item ->
                    if (insertItem(item)) {
                        successCount++
                    }
                }
                
                Log.d(TAG, "Test data initialization completed, successfully inserted $successCount items")
                return@withContext successCount > 0
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize test data", e)
                return@withContext false
            }
        }
    }
    
    private fun createTestItems(): List<Item> {
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        
        return listOf(
            Item(
                id = 1,
                title = "iPhone 13 Pro 256GB",
                price = 6999.0,
                imageUrl = "https://example.com/iphone13.jpg",
                description = "Almost new iPhone 13 Pro, no damage, battery health 95%",
                category = ItemCategory.ELECTRONICS,
                location = "Beijing Chaoyang",
                sellerId = 1001,
                sellerName = "Zhang San",
                createdAt = currentDate,
                status = ItemStatus.AVAILABLE,
                condition = ItemCondition.LIKE_NEW,
                brand = "Apple",
                views = 156,
                likes = 23
            ),
            Item(
                id = 2,
                title = "MacBook Air M1",
                price = 8500.0,
                imageUrl = "https://example.com/macbook.jpg",
                description = "2021 MacBook Air, 8GB RAM 256GB storage, light use",
                category = ItemCategory.ELECTRONICS,
                location = "Shanghai Pudong",
                sellerId = 1002,
                sellerName = "Li Si",
                createdAt = currentDate,
                status = ItemStatus.AVAILABLE,
                condition = ItemCondition.GOOD,
                brand = "Apple",
                views = 89,
                likes = 12
            ),
            Item(
                id = 3,
                title = "Nike Sports Shoes 42",
                price = 580.0,
                imageUrl = "https://example.com/nike-shoes.jpg",
                description = "Nike Air Max series, worn only few times, size runs small",
                category = ItemCategory.CLOTHING,
                location = "Guangzhou Tianhe",
                sellerId = 1003,
                sellerName = "Wang Wu",
                createdAt = currentDate,
                status = ItemStatus.AVAILABLE,
                condition = ItemCondition.GOOD,
                brand = "Nike",
                views = 67,
                likes = 8
            ),
            Item(
                id = 4,
                title = "ZARA Jacket M",
                price = 299.0,
                imageUrl = "https://example.com/zara-jacket.jpg",
                description = "Classic ZARA jacket, 90% new, no damage",
                category = ItemCategory.CLOTHING,
                location = "Shenzhen Nanshan",
                sellerId = 1004,
                sellerName = "Zhao Liu",
                createdAt = currentDate,
                status = ItemStatus.AVAILABLE,
                condition = ItemCondition.GOOD,
                brand = "ZARA",
                views = 45,
                likes = 6
            ),
            Item(
                id = 5,
                title = "Python Programming Book",
                price = 45.0,
                imageUrl = "https://example.com/python-book.jpg",
                description = "Book in good condition, with some notes",
                category = ItemCategory.BOOKS,
                location = "Hangzhou Xihu",
                sellerId = 1005,
                sellerName = "Sun Qi",
                createdAt = currentDate,
                status = ItemStatus.AVAILABLE,
                condition = ItemCondition.GOOD,
                brand = "People's Posts and Telecommunications Publishing House",
                views = 34,
                likes = 5
            )
        )
    }
}