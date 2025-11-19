package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.secondhandmarket.ItemDetailActivity
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.FavoriteManager
import com.example.secondhandmarket.data.ItemDataGenerator
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.ui.adapter.ItemAdapter
import com.google.android.material.appbar.MaterialToolbar
import android.widget.LinearLayout
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 收藏页面活动
 * 显示用户收藏的所有商品
 */
class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var favoriteManager: FavoriteManager
    private lateinit var itemAdapter: ItemAdapter
    private val favoriteItems = mutableListOf<Item>()
    
    // UI组件
    private lateinit var toolbar: MaterialToolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var emptyView: LinearLayout
    private lateinit var favoriteCountText: MaterialTextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        
        // 初始化收藏管理器
        favoriteManager = FavoriteManager(this)
        
        // 初始化视图
        initViews()
        
        // 设置工具栏
        setupToolbar()
        
        // 设置RecyclerView
        setupRecyclerView()
        
        // 设置下拉刷新
        setupSwipeRefresh()
        
        // 加载收藏数据
        loadFavoriteItems()
    }
    
    override fun onResume() {
        super.onResume()
        // 每次返回页面时刷新数据
        loadFavoriteItems()
    }
    
    /**
     * 初始化视图
     */
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        emptyView = findViewById(R.id.empty_view)
        favoriteCountText = findViewById(R.id.favorite_count_text)
    }
    
    /**
     * 设置工具栏
     */
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "我的收藏"
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        itemAdapter = ItemAdapter(favoriteItems,
            { item: Item ->
                // 处理商品点击事件
                val intent = Intent(this, ItemDetailActivity::class.java)
                intent.putExtra("ITEM_ID", item.id)
                startActivity(intent)
            },
            { item: Item, position: Int ->
                // 处理收藏按钮点击事件
                handleFavoriteClick(item, position)
            }
        )
        
        recyclerView.adapter = itemAdapter
    }
    
    /**
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            loadFavoriteItems()
        }
    }
    
    /**
     * 处理收藏按钮点击
     */
    private fun handleFavoriteClick(item: Item, position: Int) {
        try {
            val isCurrentlyFavorite = item.isFavorite
            val newFavoriteStatus = !isCurrentlyFavorite
            
            if (newFavoriteStatus) {
                // 添加到收藏
                favoriteManager.addToFavorites(item)
                Toast.makeText(this, "已添加到收藏", Toast.LENGTH_SHORT).show()
            } else {
                // 从收藏中移除
                favoriteManager.removeFromFavorites(item.id)
                Toast.makeText(this, "已从收藏中移除", Toast.LENGTH_SHORT).show()
                
                // 从列表中移除这个商品
                favoriteItems.removeAt(position)
                itemAdapter.notifyItemRemoved(position)
                itemAdapter.notifyItemRangeChanged(position, favoriteItems.size - position)
            }
            
            // 更新UI状态
            updateEmptyView()
            updateFavoriteCount()
            
        } catch (e: Exception) {
            android.util.Log.e("FavoritesActivity", "处理收藏点击失败", e)
            Toast.makeText(this, "操作失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 加载收藏的商品
     */
    private fun loadFavoriteItems() {
        lifecycleScope.launch {
            try {
                swipeRefreshLayout.isRefreshing = true
                
                val items = withContext(Dispatchers.IO) {
                    // 这里我们需要创建一个临时的挂起函数来调用generateMockItems
                    suspend fun getMockItems() = ItemDataGenerator.generateMockItems()
                    getMockItems()
                }
                
                favoriteItems.clear()
                favoriteItems.addAll(items.filter { item -> 
                    favoriteManager.isItemFavorite(item.id)
                })
                
                itemAdapter.updateItems(favoriteItems)
                updateEmptyView()
                
                Log.d("FavoritesActivity", "加载收藏列表成功，共 ${favoriteItems.size} 项")
                
            } catch (e: Exception) {
                Log.e("FavoritesActivity", "加载收藏列表失败", e)
                Toast.makeText(this@FavoritesActivity, "加载收藏列表失败：${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
    
    /**
     * 更新空视图显示
     */
    private fun updateEmptyView() {
        if (favoriteItems.isEmpty()) {
            emptyView.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            emptyView.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE
        }
    }
    
    /**
     * 更新收藏数量显示
     */
    private fun updateFavoriteCount() {
        val count = favoriteItems.size
        favoriteCountText.text = "共收藏 $count 件商品"
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