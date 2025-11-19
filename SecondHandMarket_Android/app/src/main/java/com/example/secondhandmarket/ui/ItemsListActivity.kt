package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.secondhandmarket.MainActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.ItemDataGenerator
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.ui.adapter.ItemAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ItemsListActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var toolbar: Toolbar
    private var itemList: MutableList<Item> = mutableListOf()
    private var isLoading = false
    private var page = 1
    private val pageSize = 10
    
    // 用于管理Handler任务，防止内存泄漏
    private val handler = Handler(Looper.getMainLooper())
    private val delayedTasks = mutableListOf<Runnable>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_list)
        
        // 初始化视图
        recyclerView = findViewById(R.id.recycler_view)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        toolbar = findViewById(R.id.toolbar)
        
        // 设置返回按钮
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        
        // 使用新的返回按钮处理方式
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
        
        // 设置返回按钮点击事件
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // 设置底部导航按钮点击事件
        try {
            // 设置底部导航按钮事件
            val homeButton: Button = findViewById(R.id.btn_home)
            homeButton.setOnClickListener {
                // 回到首页
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            
            val profileButton: Button = findViewById(R.id.btn_profile)
            profileButton.setOnClickListener {
                // 跳转到个人中心
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
        } catch (e: Exception) {
            // 如果按钮不存在，忽略错误
        }
        
        // 设置RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemAdapter = ItemAdapter(itemList, 
            { item ->
                // 处理商品点击事件
                Toast.makeText(this, "查看商品: ${item.title}", Toast.LENGTH_SHORT).show()
            },
            { item, position ->
                // 处理收藏按钮点击事件（这里暂时空实现）
                Toast.makeText(this, "收藏按钮点击: ${item.title}", Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = itemAdapter
        
        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
        
        // 设置上拉加载更多
        setupLoadMoreListener()
        
        // 加载初始数据
        loadInitialData()
    }
    
    private fun loadInitialData() {
        // 模拟加载初始数据
        itemList.clear()
        page = 1
        loadItems(page, pageSize)
    }
    
    private fun refreshData() {
        try {
            // 模拟刷新数据
            page = 1
            val refreshTask = Runnable {
                try {
                    loadItems(page, pageSize)
                } finally {
                    // 确保刷新状态被取消
                    swipeRefreshLayout.isRefreshing = false
                }
            }
            delayedTasks.add(refreshTask)
            handler.postDelayed(refreshTask, 1500)
        } catch (e: Exception) {
            swipeRefreshLayout.isRefreshing = false
            Toast.makeText(this, "刷新失败，请稍后重试", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadMoreData() {
        if (isLoading) return
        
        isLoading = true
        page++
        
        // 模拟加载更多数据
        val loadMoreTask = Runnable {
            try {
                loadItems(page, pageSize)
            } finally {
                // 确保加载状态被更新
                isLoading = false
            }
        }
        delayedTasks.add(loadMoreTask)
        handler.postDelayed(loadMoreTask, 1500)
    }
    
    private fun setupLoadMoreListener() {
        try {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
            // 这里应该是从服务器或本地数据库加载数据
            // 目前使用模拟数据
            val newItems = generateMockItems(page, pageSize)
            
            // 确保在主线程更新UI
            runOnUiThread {
                try {
                    if (newItems.isNotEmpty()) {
                        if (page == 1) {
                            itemList.clear()
                        }
                        
                        itemList.addAll(newItems)
                        itemAdapter.updateItems(itemList)
                    }
                    // 确保刷新状态被取消
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                    isLoading = false
                } catch (uiException: Exception) {
                    // 处理UI更新异常
                    Toast.makeText(this, "更新界面失败，请稍后重试", Toast.LENGTH_SHORT).show()
                    // 确保刷新状态被取消
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            // 加载数据失败，显示错误提示
            runOnUiThread {
                Toast.makeText(this, "加载数据失败，请稍后重试", Toast.LENGTH_SHORT).show()
                // 确保刷新状态被取消
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
                isLoading = false
            }
        }
    }
    
    private fun generateMockItems(page: Int, pageSize: Int): List<Item> {
        // 创建一个作用域来调用suspend函数
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        val newItems = mutableListOf<Item>()
        
        scope.launch {
            try {
                val items = ItemDataGenerator.generateMockItems(page, pageSize)
                newItems.addAll(items)
            } catch (e: Exception) {
                Log.e("ItemsListActivity", "生成模拟数据失败", e)
            }
        }
        
        // 等待一下让异步操作完成（临时解决方案）
        Thread.sleep(100)
        return newItems
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // 取消所有Handler任务，防止内存泄漏
        handler.removeCallbacksAndMessages(null)
        delayedTasks.clear()
        
        android.util.Log.d("ItemsListActivity", "Activity已销毁，Handler任务已清理")
    }
}