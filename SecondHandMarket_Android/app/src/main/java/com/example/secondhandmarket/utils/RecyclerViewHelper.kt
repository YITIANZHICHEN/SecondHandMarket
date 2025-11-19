package com.example.secondhandmarket.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

/**
 * RecyclerView辅助工具类
 * 提供通用的RecyclerView设置和加载更多功能
 */
object RecyclerViewHelper {
    
    /**
     * 设置RecyclerView的基本配置
     */
    fun setupRecyclerView(
        recyclerView: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        context: Context
    ) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)
    }
    
    /**
     * 设置下拉刷新
     */
    fun setupSwipeRefresh(
        swipeRefreshLayout: SwipeRefreshLayout,
        onRefresh: () -> Unit
    ) {
        swipeRefreshLayout.setOnRefreshListener {
            onRefresh()
        }
    }
    
    /**
     * 设置加载更多监听器
     */
    fun setupLoadMoreListener(
        recyclerView: RecyclerView,
        isLoading: Boolean,
        onLoadMore: () -> Unit
    ) {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                if (isLoading) return
                
                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                
                // 当滚动到最后一个可见项时加载更多
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && 
                    firstVisibleItemPosition >= 0 && 
                    totalItemCount >= 10) {
                    onLoadMore()
                }
            }
        })
    }
    
    /**
     * 通用的加载状态管理
     */
    class LoadStateManager {
        var isLoading = false
        var page = 1
        val pageSize = 10
        
        fun reset() {
            page = 1
            isLoading = false
        }
        
        fun startLoading() {
            isLoading = true
        }
        
        fun finishLoading() {
            isLoading = false
        }
        
        fun incrementPage() {
            page++
        }
    }
}