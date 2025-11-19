package com.example.secondhandmarket

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.secondhandmarket.data.FavoriteManager
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.data.model.ItemCondition
import com.example.secondhandmarket.data.model.ItemStatus
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class ItemDetailActivity : AppCompatActivity() {

    private var currentItem: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        try {
            // 接收传递的商品数据
            val itemId = intent.getLongExtra("ITEM_ID", -1)
            val itemTitle = intent.getStringExtra("ITEM_TITLE") ?: ""
            val itemPrice = intent.getDoubleExtra("ITEM_PRICE", 0.0)
            val itemDescription = intent.getStringExtra("ITEM_DESCRIPTION") ?: ""
            val itemCategory = intent.getStringExtra("ITEM_CATEGORY") ?: ""
            val itemLocation = intent.getStringExtra("ITEM_LOCATION") ?: ""
            val itemSellerName = intent.getStringExtra("ITEM_SELLER_NAME") ?: ""
            val itemStatus = intent.getStringExtra("ITEM_STATUS") ?: ""
            val itemCondition = intent.getStringExtra("ITEM_CONDITION") ?: ""
            val itemBrand = intent.getStringExtra("ITEM_BRAND") ?: ""
            val itemViews = intent.getIntExtra("ITEM_VIEWS", 0)
            val itemLikes = intent.getIntExtra("ITEM_LIKES", 0)

            // 创建Item对象
            val category = try {
                ItemCategory.valueOf(itemCategory)
            } catch (e: Exception) {
                ItemCategory.OTHER
            }

            val status = try {
                ItemStatus.valueOf(itemStatus)
            } catch (e: Exception) {
                ItemStatus.AVAILABLE
            }

            val condition = try {
                ItemCondition.valueOf(itemCondition)
            } catch (e: Exception) {
                ItemCondition.GOOD
            }

            currentItem = Item(
                id = itemId,
                title = itemTitle,
                price = itemPrice,
                imageUrl = "",
                description = itemDescription,
                category = category,
                location = itemLocation,
                sellerName = itemSellerName,
                status = status,
                condition = condition,
                brand = itemBrand,
                views = itemViews,
                likes = itemLikes
            )

            // 设置工具栏
            setupToolbar()

            // 填充商品信息
            setupItemDetails()

            // 设置按钮点击事件
            setupButtons()

            android.util.Log.d("ItemDetailActivity", "商品详情页面已加载: $itemTitle")

        } catch (e: Exception) {
            android.util.Log.e("ItemDetailActivity", "加载商品详情失败", e)
            Toast.makeText(this, "加载商品详情失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "商品详情"
    }

    private fun setupItemDetails() {
        currentItem?.let { item ->
            try {
                // 设置商品图片（使用分类对应的图标）
                val itemImage = findViewById<ImageView>(R.id.item_image)
                val imageResId = when (item.category) {
                    ItemCategory.ELECTRONICS -> R.drawable.ic_electronics
                    ItemCategory.CLOTHING -> R.drawable.button_rounded
                    ItemCategory.BOOKS -> R.drawable.category_normal_bg
                    ItemCategory.HOME -> R.drawable.brand_bg
                    ItemCategory.SPORTS -> R.drawable.condition_bg
                    ItemCategory.BEAUTY -> R.drawable.button_border
                    ItemCategory.TOYS -> R.drawable.category_selected_bg
                    ItemCategory.DIGITAL -> R.drawable.ic_launcher_foreground
                    ItemCategory.FURNITURE -> R.drawable.button_red_rounded
                    ItemCategory.OTHER -> R.drawable.ic_launcher_background
                    else -> R.drawable.ic_launcher_background
                }
                itemImage.setImageResource(imageResId)

                // 设置商品标题
                findViewById<TextView>(R.id.item_title).text = item.title

                // 设置商品价格
                findViewById<TextView>(R.id.item_price).text = "￥${item.price}"

                // 设置商品状态
                val statusText = findViewById<TextView>(R.id.item_status)
                when (item.status) {
                    ItemStatus.AVAILABLE -> {
                        statusText.text = "可购买"
                        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                        statusText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
                    }
                    ItemStatus.SOLD -> {
                        statusText.text = "已售出"
                        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                        statusText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_light))
                    }
                    ItemStatus.RESERVED -> {
                        statusText.text = "已预订"
                        statusText.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
                        statusText.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
                    }
                }

                // 设置浏览量
                findViewById<TextView>(R.id.item_views).text = "${item.views}次浏览"

                // 设置商品分类
                val categoryName = when (item.category) {
                    ItemCategory.ELECTRONICS -> "电子产品"
                    ItemCategory.CLOTHING -> "服装鞋帽"
                    ItemCategory.BOOKS -> "图书文具"
                    ItemCategory.HOME -> "家居用品"
                    ItemCategory.SPORTS -> "运动户外"
                    ItemCategory.BEAUTY -> "美妆个护"
                    ItemCategory.TOYS -> "玩具游戏"
                    ItemCategory.DIGITAL -> "数码配件"
                    ItemCategory.FURNITURE -> "家具家电"
                    ItemCategory.OTHER -> "其他"
                }
                findViewById<TextView>(R.id.item_category).text = categoryName

                // 设置商品成色
                val conditionName = when (item.condition) {
                    ItemCondition.NEW -> "全新"
                    ItemCondition.LIKE_NEW -> "几乎全新"
                    ItemCondition.GOOD -> "良好"
                    ItemCondition.FAIR -> "一般"
                    ItemCondition.POOR -> "较差"
                }
                findViewById<TextView>(R.id.item_condition).text = conditionName

                // 设置品牌信息
                findViewById<TextView>(R.id.item_brand).text = "品牌：${item.brand.ifEmpty { "未知" }}"

                // 设置商品描述
                findViewById<TextView>(R.id.item_description).text = item.description.ifEmpty { "暂无描述" }

                // 设置卖家信息
                findViewById<TextView>(R.id.seller_name).text = item.sellerName.ifEmpty { "匿名用户" }
                findViewById<TextView>(R.id.seller_location).text = item.location

                android.util.Log.d("ItemDetailActivity", "商品详情填充完成: ${item.title}")

            } catch (e: Exception) {
                android.util.Log.e("ItemDetailActivity", "填充商品详情失败", e)
                Toast.makeText(this, "显示商品信息失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        try {
            // 收藏按钮
            val btnFavorite = findViewById<MaterialButton>(R.id.btn_favorite)
            btnFavorite.setOnClickListener {
                try {
                    currentItem?.let { item ->
                        val favoriteManager = FavoriteManager(this)
                        val isCurrentlyFavorite = favoriteManager.isItemFavorite(item.id)
                        val newFavoriteStatus = !isCurrentlyFavorite
                        
                        if (newFavoriteStatus) {
                            favoriteManager.addToFavorites(item)
                            Toast.makeText(this, "已添加到收藏", Toast.LENGTH_SHORT).show()
                        } else {
                            favoriteManager.removeFromFavorites(item.id)
                            Toast.makeText(this, "已从收藏中移除", Toast.LENGTH_SHORT).show()
                        }
                        
                        // 更新按钮状态和文本
                        updateFavoriteButtonState(newFavoriteStatus)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("ItemDetailActivity", "收藏操作失败", e)
                    Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show()
                }
            }

            // 联系卖家按钮
            val btnContactSeller = findViewById<MaterialButton>(R.id.btn_contact_seller)
            btnContactSeller.setOnClickListener {
                currentItem?.let { item ->
                    try {
                        // 启动聊天Activity
                        val intent = Intent(this, ChatActivity::class.java).apply {
                            putExtra("seller_name", item.sellerName)
                            putExtra("item_data", item)
                        }
                        startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("ItemDetailActivity", "启动聊天界面失败", e)
                        Toast.makeText(this, "启动聊天失败，请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("ItemDetailActivity", "设置按钮事件失败", e)
        }
    }

    private fun updateFavoriteButtonState(isFavorite: Boolean) {
        val btnFavorite = findViewById<MaterialButton>(R.id.btn_favorite)
        if (isFavorite) {
            btnFavorite.text = "已收藏"
            btnFavorite.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_filled))
        } else {
            btnFavorite.text = "收藏"
            btnFavorite.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_border))
        }
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
}