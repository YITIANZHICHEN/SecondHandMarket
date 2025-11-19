package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.ItemCategory
import com.example.secondhandmarket.ui.adapter.CategoryAdapter

class CategoryFilterActivity : AppCompatActivity() {
    
    private lateinit var categoryGridView: GridView
    private lateinit var toolbar: Toolbar
    private lateinit var confirmButton: Button
    private lateinit var clearButton: Button
    
    private var selectedCategory: ItemCategory? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_filter)
        
        // 初始化视图
        categoryGridView = findViewById(R.id.category_grid_view)
        toolbar = findViewById(R.id.toolbar)
        confirmButton = findViewById(R.id.btn_confirm)
        clearButton = findViewById(R.id.btn_clear)
        
        // 设置Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "选择分类"
        
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
        
        // 设置分类适配器
        val categories = listOf(
            ItemCategory.ELECTRONICS,
            ItemCategory.CLOTHING,
            ItemCategory.BOOKS,
            ItemCategory.HOME,
            ItemCategory.SPORTS,
            ItemCategory.BEAUTY,
            ItemCategory.TOYS,
            ItemCategory.DIGITAL,
            ItemCategory.FURNITURE,
            ItemCategory.OTHER
        )
        
        val categoryAdapter = CategoryAdapter(this, categories) { category ->
            selectedCategory = category
            Toast.makeText(this, "已选择: ${getCategoryName(category)}", Toast.LENGTH_SHORT).show()
        }
        categoryGridView.adapter = categoryAdapter
        
        // 设置确认按钮点击事件
        confirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_CATEGORY", selectedCategory?.name)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        
        // 设置清除按钮点击事件
        clearButton.setOnClickListener {
            selectedCategory = null
            categoryAdapter.clearSelection()
            Toast.makeText(this, "已清除选择", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun getCategoryName(category: ItemCategory): String {
        return when (category) {
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
    }
}