package com.example.secondhandmarket

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.secondhandmarket.data.ItemDataGenerator
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.ui.CategoryFilterActivity
import com.example.secondhandmarket.ui.ProfileActivity
import com.example.secondhandmarket.ui.adapter.ItemAdapter
import com.example.secondhandmarket.data.FavoriteManager
import com.example.secondhandmarket.utils.GraphicsUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    companion object {
        // 移除不必要的权限请求，应用功能不需要相机和存储权限
        private const val REQUEST_CODE_CATEGORY_FILTER = 1001
        
        // 用于DatabaseManager获取Context
        private var instance: Context? = null
        
        @JvmStatic
        fun getContext(): Context? {
            return instance
        }
    }
    
    private var usernameTextView: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var toolbar: Toolbar? = null
    // RecyclerView适配器（确保始终有有效的适配器）
    private var itemAdapter: ItemAdapter = ItemAdapter(emptyList(), 
        // 商品点击事件 - 空实现
        { _ -> },
        // 收藏点击事件 - 空实现  
        { _, _ -> }
    )
    // 底部导航按钮
    private var btnHome: Button? = null
    private var btnCategory: Button? = null
    private var btnFavorite: Button? = null
    private var btnProfile: Button? = null
    
    // 收藏管理器
    private var favoriteManager: FavoriteManager? = null
    private var itemList: MutableList<Item> = mutableListOf()
    private var isLoading = false
    private var page = 1
    private val pageSize = 10
    private var currentUsername: String? = null
    
    // 用于管理Handler任务，防止内存泄漏
    private val handler = Handler(Looper.getMainLooper())
    private val delayedTasks = mutableListOf<Runnable>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置MainActivity实例，供DatabaseManager使用
        instance = this
        
        // 检查是否从登录页面跳转过来
        currentUsername = intent.getStringExtra("USERNAME")
        
        // 移除强制登录检查，允许用户直接查看商品列表
        // 如果有登录信息则显示用户信息，如果没有则以游客身份使用
        android.util.Log.d("MainActivity", "应用启动 - 用户名: ${currentUsername ?: "游客"}")
        
        setContentView(R.layout.activity_main)
        
        // 立即应用图形渲染优化设置，解决OpenGL/EGL警告问题
        GraphicsUtils.applyAllOptimizations(this)
        
        // 专门处理swap behavior错误，防止"Unable to match the desired swap behavior"警告
        GraphicsUtils.handleSwapBehaviorError(this)
        android.util.Log.d("MainActivity", "所有图形渲染优化设置已应用")
        
        // 立即初始化视图，避免重复初始化
        try {
            android.util.Log.d("MainActivity", "开始初始化视图组件")
            usernameTextView = findViewById(R.id.username_text)
            recyclerView = findViewById(R.id.recycler_view)
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
            toolbar = findViewById(R.id.toolbar)
            
            android.util.Log.d("MainActivity", "视图初始化完成，开始检查关键组件")
            
            // 检查关键视图是否都成功初始化
            if (usernameTextView == null || recyclerView == null || swipeRefreshLayout == null || toolbar == null) {
                android.util.Log.e("MainActivity", "关键视图初始化失败，无法继续运行")
                Toast.makeText(this, "应用初始化失败，请重启应用", Toast.LENGTH_LONG).show()
                // 延迟后关闭应用
                val finishTask = Runnable {
                    finish()
                }
                delayedTasks.add(finishTask)
                handler.postDelayed(finishTask, 2000)
                return
            }
            
            android.util.Log.d("MainActivity", "关键组件检查通过，开始数据库初始化")
            
            // 初始化底部导航按钮
            btnHome = findViewById(R.id.btn_home)
            btnCategory = findViewById(R.id.btn_category)
            btnFavorite = findViewById(R.id.btn_favorite)
            btnProfile = findViewById(R.id.btn_profile)
            
            android.util.Log.d("MainActivity", "底部导航按钮初始化完成")
            
            // 视图初始化成功，直接初始化应用（不需要权限）
            initializeDatabase()
            
        } catch (e: Exception) {
            // 如果视图初始化失败，记录错误并关闭应用
            android.util.Log.e("MainActivity", "视图初始化失败", e)
            Toast.makeText(this, "应用初始化失败，请重启应用", Toast.LENGTH_LONG).show()
            // 延迟后关闭应用
            val finishTask = Runnable {
                finish()
            }
            delayedTasks.add(finishTask)
            handler.postDelayed(finishTask, 2000)
        }
    }
    
    /**
     * 初始化MySQL数据库连接和测试数据
     */
    private fun initializeDatabase() {
        android.util.Log.d("MainActivity", "开始初始化MySQL数据库...")
        
        try {
            // 使用协程初始化数据库
            lifecycleScope.launch {
                try {
                    android.util.Log.d("MainActivity", "开始协程初始化数据库...")
                    
                    // 在IO协程中初始化数据库
                    val success = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            ItemDataGenerator.initializeDatabase(this@MainActivity)
                            android.util.Log.d("MainActivity", "数据库初始化完成")
                            true
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "数据库初始化失败", e)
                            false
                        }
                    }
                    
                    if (success) {
                        android.util.Log.d("MainActivity", "数据库初始化成功，开始初始化应用...")
                    } else {
                        android.util.Log.w("MainActivity", "数据库初始化失败，将使用本地数据...")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "数据库连接失败，使用本地数据", Toast.LENGTH_SHORT).show()
                        }
                    }
                    
                    // 继续初始化应用
                    initializeApp()
                    
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "数据库初始化协程异常", e)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "数据库初始化失败，使用本地数据", Toast.LENGTH_SHORT).show()
                    }
                    initializeApp()
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "启动数据库初始化失败", e)
            // 直接初始化应用
            initializeApp()
        }
    }
    
    private fun loadInitialData() {
        android.util.Log.d("MainActivity", "开始加载初始数据")
        itemList.clear()
        page = 1
        loadItems(page, pageSize)
    }
    
    private fun refreshData() {
        try {
            android.util.Log.d("MainActivity", "开始刷新数据")
            page = 1
            
            // 使用类属性favoriteManager
            
            lifecycleScope.launch {
                try {
                    swipeRefreshLayout?.isRefreshing = true
                    
                    val newItems = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        ItemDataGenerator.generateMockItems(page, pageSize)
                    }
                    
                    // 更新商品的收藏状态
                    val itemsWithFavoriteStatus = newItems.map { item ->
                        item.copy(isFavorite = favoriteManager?.isItemFavorite(item.id) ?: false)
                    }
                    
                    android.util.Log.d("MainActivity", "刷新获取到${newItems.size}条数据，已更新收藏状态")
                    
                    itemList.clear()
                    itemList.addAll(itemsWithFavoriteStatus)
                    
                    runOnUiThread {
                        if (itemList.isNotEmpty()) {
                            itemAdapter.updateItems(itemList)
                            android.util.Log.d("MainActivity", "刷新完成，列表已更新")
                            Toast.makeText(this@MainActivity, "刷新成功", Toast.LENGTH_SHORT).show()
                        } else {
                            android.util.Log.e("MainActivity", "刷新后列表仍为空")
                            Toast.makeText(this@MainActivity, "暂无数据", Toast.LENGTH_SHORT).show()
                        }
                        swipeRefreshLayout?.isRefreshing = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "刷新数据失败", e)
                    runOnUiThread {
                        swipeRefreshLayout?.isRefreshing = false
                        Toast.makeText(this@MainActivity, "刷新失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            swipeRefreshLayout?.isRefreshing = false
            Toast.makeText(this, "刷新失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadMoreData() {
        if (isLoading) return
        
        isLoading = true
        page++
        
        android.util.Log.d("MainActivity", "开始加载更多数据，第${page}页")
        
        lifecycleScope.launch {
            try {
                val newItems = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    ItemDataGenerator.generateMockItems(page, pageSize)
                }
                
                android.util.Log.d("MainActivity", "加载更多获取到${newItems.size}条数据")
                
                if (newItems.isNotEmpty()) {
                    itemList.addAll(newItems)
                    
                    runOnUiThread {
                        itemAdapter.updateItems(itemList)
                        android.util.Log.d("MainActivity", "加载更多完成，当前列表${itemList.size}条")
                    }
                } else {
                    android.util.Log.d("MainActivity", "没有更多数据可加载")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "没有更多数据了", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "加载更多数据失败", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "加载更多失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun setupLoadMoreListener() {
        try {
            recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    try {
                        super.onScrolled(recyclerView, dx, dy)
                        
                        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                        val visibleItemCount = layoutManager.childCount
                        val totalItemCount = layoutManager.itemCount
                        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                        
                        // 当滚动到最后一个可见项时加载更多
                        if (!isLoading && 
                            visibleItemCount + firstVisibleItemPosition >= totalItemCount && 
                            firstVisibleItemPosition >= 0 && 
                            totalItemCount >= pageSize) {
                            loadMoreData()
                        }
                    } catch (e: Exception) {
                        // 忽略滚动事件异常
                    }
                }
            })
        } catch (e: Exception) {
            // 忽略添加滚动监听器异常
        }
    }
    
    private fun loadItems(page: Int, pageSize: Int) {
        try {
            showLoading(true)
            
            // 初始化收藏管理器
            val favoriteManager = FavoriteManager(this)
            
            // 使用协程异步加载数据
            lifecycleScope.launch {
                try {
                    android.util.Log.d("MainActivity", "开始异步加载商品数据，第${page}页，每页${pageSize}条")
                    
                    // 从数据库异步获取数据
                    val newItems = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        ItemDataGenerator.generateMockItems(page, pageSize)
                    }
                    
                    android.util.Log.d("MainActivity", "从数据库获取到${newItems.size}条新商品数据")
                    
                    // 更新商品的收藏状态
                    val itemsWithFavoriteStatus = newItems.map { item ->
                        item.copy(isFavorite = favoriteManager.isItemFavorite(item.id))
                    }
                    
                    android.util.Log.d("MainActivity", "已更新${itemsWithFavoriteStatus.size}个商品的收藏状态")
                    
                    // 如果是第一页，先清空列表
                    if (page == 1) {
                        itemList.clear()
                        android.util.Log.d("MainActivity", "已清空列表")
                    }
                    
                    // 添加新数据
                    itemList.addAll(itemsWithFavoriteStatus)
                    
                    android.util.Log.d("MainActivity", "当前列表共有${itemList.size}条数据")
                    
                    // 在主线程更新UI
                    runOnUiThread {
                        android.util.Log.d("MainActivity", "开始更新UI，当前列表有${itemList.size}条数据")
                        android.util.Log.d("MainActivity", "准备调用itemAdapter.updateItems()，传入列表大小: ${itemList.size}")
                        
                        if (itemList.isNotEmpty()) {
                            itemAdapter.updateItems(itemList)
                            android.util.Log.d("MainActivity", "已调用updateItems更新列表")
                        } else {
                            android.util.Log.e("MainActivity", "商品列表为空，请检查数据库连接")
                            Toast.makeText(this@MainActivity, "商品列表为空，请检查网络连接", Toast.LENGTH_SHORT).show()
                        }
                        
                        showLoading(false)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "异步加载数据时发生错误", e)
                    
                    runOnUiThread {
                        showLoading(false)
                        Toast.makeText(this@MainActivity, "加载数据失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "启动数据加载时发生错误", e)
            showLoading(false)
        }
    }
    
    private fun showLoading(show: Boolean) {
        val progressBar = findViewById<ProgressBar>(R.id.progress_bar)
        if (show) {
            progressBar?.visibility = android.view.View.VISIBLE
            android.util.Log.d("MainActivity", "显示加载进度条")
        } else {
            progressBar?.visibility = android.view.View.GONE
            android.util.Log.d("MainActivity", "隐藏加载进度条")
        }
    }
    
    private fun initializeApp() {
        // 视图已经在onCreate中检查过，这里直接进行初始化
        android.util.Log.d("MainActivity", "=== initializeApp()方法被调用 ===")
        android.util.Log.d("MainActivity", "开始初始化应用组件")
        
        try {
            // 使用保存的用户名，如果没有则以游客身份显示
            if (!currentUsername.isNullOrEmpty()) {
                usernameTextView?.text = "欢迎您，$currentUsername"
            } else {
                usernameTextView?.text = "欢迎您，游客用户"
            }
            
            // 设置Toolbar
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.title = "二手市场"
            
            // 设置搜索功能
            setupSearchView()
            
            // 设置底部导航按钮点击事件
            try {
                // 设置底部导航按钮事件，添加null安全检查
                val homeButton: Button? = findViewById(R.id.btn_home)
                homeButton?.setOnClickListener {
                    // 刷新首页数据
                    refreshData()
                    Toast.makeText(this, "刷新首页", Toast.LENGTH_SHORT).show()
                }
                
                val categoryButton: Button? = findViewById(R.id.btn_category)
                categoryButton?.setOnClickListener {
                    // 跳转到分类筛选页面
                    val intent = Intent(this, CategoryFilterActivity::class.java)
                    startActivityForResult(intent, REQUEST_CODE_CATEGORY_FILTER)
                }
                
                val favoriteButton: Button? = findViewById(R.id.btn_favorite)
                favoriteButton?.setOnClickListener {
                    // 跳转到收藏页面
                    val intent = Intent(this, com.example.secondhandmarket.ui.FavoritesActivity::class.java)
                    startActivity(intent)
                }
                
                val profileButton: Button? = findViewById(R.id.btn_profile)
                profileButton?.setOnClickListener {
                    // 跳转到个人中心
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USERNAME", currentUsername)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                // 如果按钮不存在，忽略错误
                android.util.Log.e("MainActivity", "底部导航按钮设置失败: ${e.message}")
            }
            
            // 设置RecyclerView
            try {
                // 首先设置LayoutManager
                recyclerView?.layoutManager = LinearLayoutManager(this)
                
                // 初始化收藏管理器
                favoriteManager = FavoriteManager(this)
                
                // 立即设置空适配器到RecyclerView，避免"no adapter attached"警告
                android.util.Log.d("MainActivity", "设置RecyclerView初始适配器")
                recyclerView?.adapter = itemAdapter
                
                // 延迟配置适配器的实际事件处理逻辑，避免初始化时未就绪
                Handler(Looper.getMainLooper()).postDelayed({
                    try {
                        android.util.Log.d("MainActivity", "开始配置RecyclerView完整事件处理")
                        
                        // 更新适配器的事件处理逻辑
                        itemAdapter = ItemAdapter(itemList, 
                            // 商品点击事件
                            { item ->
                                try {
                                    val intent = Intent(this@MainActivity, ItemDetailActivity::class.java).apply {
                                        putExtra("ITEM_ID", item.id)
                                        putExtra("ITEM_TITLE", item.title)
                                        putExtra("ITEM_PRICE", item.price)
                                        putExtra("ITEM_DESCRIPTION", item.description)
                                        putExtra("ITEM_CATEGORY", item.category.name)
                                        putExtra("ITEM_LOCATION", item.location)
                                        putExtra("ITEM_SELLER_NAME", item.sellerName)
                                        putExtra("ITEM_STATUS", item.status.name)
                                        putExtra("ITEM_CONDITION", item.condition.name)
                                        putExtra("ITEM_BRAND", item.brand)
                                        putExtra("ITEM_VIEWS", item.views)
                                        putExtra("ITEM_LIKES", item.likes)
                                    }
                                    startActivity(intent)
                                    android.util.Log.d("MainActivity", "跳转到商品详情页: ${item.title}")
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "跳转商品详情页失败", e)
                                    Toast.makeText(this@MainActivity, "无法打开商品详情", Toast.LENGTH_SHORT).show()
                                }
                            },
                            // 收藏点击事件
                            { item, position ->
                                try {
                                    val itemId = item.id
                                    val currentFavoriteStatus = item.isFavorite
                                    val newFavoriteStatus = !currentFavoriteStatus
                                    
                                    android.util.Log.d("MainActivity", "商品 ${item.title} 收藏状态: $currentFavoriteStatus -> $newFavoriteStatus")
                                    
                                    if (newFavoriteStatus) {
                                        // 添加到收藏
                                        favoriteManager?.addToFavorites(item)
                                        Toast.makeText(this, "已添加到收藏", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // 从收藏中移除
                                        favoriteManager?.removeFromFavorites(itemId)
                                        Toast.makeText(this, "已从收藏中移除", Toast.LENGTH_SHORT).show()
                                    }
                                    
                                    // 更新适配器中的状态
                                    itemAdapter.updateFavoriteStatus(itemId, newFavoriteStatus)
                                    
                                    android.util.Log.d("MainActivity", "收藏状态已更新: ${item.title}")
                                } catch (e: Exception) {
                                    android.util.Log.e("MainActivity", "处理收藏点击事件失败", e)
                                    Toast.makeText(this, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        
                        // 重新设置适配器到RecyclerView
                        recyclerView?.adapter = itemAdapter
                        android.util.Log.d("MainActivity", "RecyclerView完整适配器配置完成")
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "RecyclerView适配器事件配置失败", e)
                        // 即使事件配置失败，空适配器仍然有效，不会导致警告
                    }
                }, 50) // 进一步减少延迟时间到50ms，提高响应速度
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "RecyclerView设置失败: ${e.message}")
                // 即使RecyclerView设置失败，也继续运行应用
            }
            
            // 设置下拉刷新
            swipeRefreshLayout?.setOnRefreshListener {
                refreshData()
            }
            
            // 设置上拉加载更多
            setupLoadMoreListener()
            
            // 加载初始数据
            loadInitialData()
            
            // 设置底部导航
            setupBottomNavigation()
            
            android.util.Log.d("MainActivity", "应用已完全初始化")
            
        } catch (e: Exception) {
            // 如果初始化过程中出现异常，记录错误但继续运行应用
            android.util.Log.e("MainActivity", "应用初始化过程中出现异常", e)
            Toast.makeText(this, "应用部分功能初始化失败，但可以继续使用", Toast.LENGTH_SHORT).show()
        }
    }
     
     override fun onDestroy() {
         super.onDestroy()
         
         // 取消所有Handler任务，防止内存泄漏
         handler.removeCallbacksAndMessages(null)
         delayedTasks.clear()
         
         android.util.Log.d("MainActivity", "Activity已销毁，Handler任务已清理")
     }
     
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         
         if (requestCode == REQUEST_CODE_CATEGORY_FILTER && resultCode == RESULT_OK) {
             val selectedCategory = data?.getStringExtra("SELECTED_CATEGORY")
             if (selectedCategory != null) {
                 val category = try {
                     ItemCategory.valueOf(selectedCategory)
                 } catch (e: IllegalArgumentException) {
                     null
                 }
                 
                 if (category != null) {
                     // 使用协程异步筛选商品
                     lifecycleScope.launch {
                         try {
                             android.util.Log.d("MainActivity", "开始筛选分类: $category")
                             
                             val filteredItems = withContext(kotlinx.coroutines.Dispatchers.IO) {
                                  ItemDataGenerator.getItemsByCategory(category, 1, pageSize)
                              }
                              
                             android.util.Log.d("MainActivity", "筛选到${filteredItems.size}条数据")
                             
                             itemList.clear()
                             itemList.addAll(filteredItems)
                             itemAdapter.updateItems(itemList)
                             
                             val categoryName = when (category) {
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
                             
                             runOnUiThread {
                                 Toast.makeText(this@MainActivity, "已筛选: $categoryName (${filteredItems.size}条)", Toast.LENGTH_SHORT).show()
                             }
                             
                         } catch (e: Exception) {
                             android.util.Log.e("MainActivity", "筛选分类失败", e)
                             runOnUiThread {
                                 Toast.makeText(this@MainActivity, "筛选失败: ${e.message}", Toast.LENGTH_SHORT).show()
                             }
                         }
                     }
                     
                     // 更新底部分类导航状态
                     updateBottomNavigationState(1)
                 }
             } else {
                 // 清除筛选，显示所有商品
                 loadInitialData()
                 Toast.makeText(this, "已清除筛选", Toast.LENGTH_SHORT).show()
                 // 回到首页状态
                 updateBottomNavigationState(0)
             }
         }
     }
     
     /**
      * 设置搜索功能
      */
     private fun setupSearchView() {
         try {
             val searchView = findViewById<SearchView>(R.id.search_view)
             if (searchView == null) {
                 android.util.Log.w("MainActivity", "未找到搜索框组件")
                 return
             }
             
              // 设置搜索框属性
             searchView.queryHint = "搜索商品..."
             searchView.isSubmitButtonEnabled = true
             
             // 设置搜索监听器
             searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                 override fun onQueryTextSubmit(query: String?): Boolean {
                     // 当用户点击提交按钮时触发
                     if (!query.isNullOrEmpty()) {
                         android.util.Log.d("MainActivity", "搜索提交: $query")
                         performSearch(query)
                         searchView.clearFocus() // 隐藏键盘
                     }
                     return true
                 }
                 
                 override fun onQueryTextChange(newText: String?): Boolean {
                     // 当搜索框文本改变时触发（实时搜索）
                     if (!newText.isNullOrEmpty() && newText.length >= 2) {
                         android.util.Log.d("MainActivity", "实时搜索: $newText")
                         performSearch(newText)
                     } else if (newText.isNullOrEmpty()) {
                         // 如果搜索框为空，显示所有商品
                         android.util.Log.d("MainActivity", "搜索框清空，显示所有商品")
                         loadInitialData()
                     }
                     return true
                 }
             })
             
             android.util.Log.d("MainActivity", "搜索功能设置完成")
             
         } catch (e: Exception) {
             android.util.Log.e("MainActivity", "设置搜索功能失败", e)
         }
     }
     
     /**
      * 执行搜索操作
      */
     private fun performSearch(keyword: String) {
         try {
             android.util.Log.d("MainActivity", "开始执行搜索: $keyword")
             
             // 显示搜索状态提示
             Toast.makeText(this, "搜索中: $keyword", Toast.LENGTH_SHORT).show()
             
             // 使用协程异步搜索
             lifecycleScope.launch {
                 try {
                     val searchResults = withContext(kotlinx.coroutines.Dispatchers.IO) {
                         ItemDataGenerator.searchItems(keyword, 1, pageSize)
                     }
                     
                     android.util.Log.d("MainActivity", "搜索完成，找到 ${searchResults.size} 个结果")
                     
                     runOnUiThread {
                         if (searchResults.isNotEmpty()) {
                             itemList.clear()
                             itemList.addAll(searchResults)
                             itemAdapter.updateItems(itemList)
                             Toast.makeText(this@MainActivity, 
                                 "找到 ${searchResults.size} 个相关商品", Toast.LENGTH_SHORT).show()
                         } else {
                             itemList.clear()
                             itemAdapter.updateItems(itemList)
                             Toast.makeText(this@MainActivity, 
                                 "未找到包含 '$keyword' 的商品", Toast.LENGTH_SHORT).show()
                         }
                     }
                     
                 } catch (e: Exception) {
                     android.util.Log.e("MainActivity", "搜索过程中发生异常", e)
                     runOnUiThread {
                         Toast.makeText(this@MainActivity, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
                         // 搜索失败时显示所有商品
                         loadInitialData()
                     }
                 }
             }
             
         } catch (e: Exception) {
             android.util.Log.e("MainActivity", "启动搜索时发生异常", e)
             Toast.makeText(this, "搜索失败，请稍后重试", Toast.LENGTH_SHORT).show()
         }
     }
     
     /**
      * 设置底部导航栏功能
      */
     private fun setupBottomNavigation() {
         try {
             android.util.Log.d("MainActivity", "开始设置底部导航")
             
             // 设置首页按钮（当前页面，保持高亮）
             btnHome?.setOnClickListener {
                 android.util.Log.d("MainActivity", "点击首页按钮")
                 Toast.makeText(this, "已在首页", Toast.LENGTH_SHORT).show()
                 updateBottomNavigationState(0)
             }
             
             // 设置分类按钮，使用startActivityForResult以便处理筛选结果
             btnCategory?.setOnClickListener {
                 android.util.Log.d("MainActivity", "点击分类按钮")
                 // 跳转到分类筛选页面
                 val intent = Intent(this, CategoryFilterActivity::class.java)
                 startActivityForResult(intent, REQUEST_CODE_CATEGORY_FILTER)
             }
             
             // 设置收藏按钮
             btnFavorite?.setOnClickListener {
                 android.util.Log.d("MainActivity", "点击收藏按钮")
                 // 跳转到收藏页面
                 val intent = Intent(this, com.example.secondhandmarket.ui.FavoritesActivity::class.java)
                 startActivity(intent)
                 updateBottomNavigationState(2)
             }
             
             // 设置个人中心按钮
             btnProfile?.setOnClickListener {
                 android.util.Log.d("MainActivity", "点击个人中心按钮")
                 navigateToProfile()
                 updateBottomNavigationState(3)
             }
             
             // 初始化状态（首页高亮）
             updateBottomNavigationState(0)
             
             android.util.Log.d("MainActivity", "底部导航设置完成")
             
         } catch (e: Exception) {
             android.util.Log.e("MainActivity", "设置底部导航失败", e)
         }
     }
     
     /**
      * 更新底部导航状态
      * @param selectedIndex 选中的按钮索引：0-首页，1-分类，2-收藏，3-我的
      */
     private fun updateBottomNavigationState(selectedIndex: Int) {
         try {
             // 重置所有按钮颜色
             btnHome?.setTextColor(getColor(R.color.text_secondary))
             btnCategory?.setTextColor(getColor(R.color.text_secondary))
             btnFavorite?.setTextColor(getColor(R.color.text_secondary))
             btnProfile?.setTextColor(getColor(R.color.text_secondary))
             
             // 设置选中按钮颜色
             when (selectedIndex) {
                 0 -> btnHome?.setTextColor(getColor(R.color.primary_color))
                 1 -> btnCategory?.setTextColor(getColor(R.color.primary_color))
                 2 -> btnFavorite?.setTextColor(getColor(R.color.primary_color))
                 3 -> btnProfile?.setTextColor(getColor(R.color.primary_color))
             }
             
             android.util.Log.d("MainActivity", "底部导航状态已更新到: $selectedIndex")
             
         } catch (e: Exception) {
             android.util.Log.e("MainActivity", "更新底部导航状态失败", e)
         }
     }
     
     /**
      * 跳转到个人中心页面
      */
     private fun navigateToProfile() {
         try {
             android.util.Log.d("MainActivity", "开始跳转到个人中心")
             
             val intent = Intent(this, ProfileActivity::class.java).apply {
                 // 传递用户名信息到个人中心
                 putExtra("USERNAME", currentUsername)
             }
             
             startActivity(intent)
             android.util.Log.d("MainActivity", "已跳转到个人中心页面")
             
         } catch (e: Exception) {
             android.util.Log.e("MainActivity", "跳转个人中心失败", e)
             Toast.makeText(this, "无法打开个人中心", Toast.LENGTH_SHORT).show()
         }
     }
     

     


     

     

}