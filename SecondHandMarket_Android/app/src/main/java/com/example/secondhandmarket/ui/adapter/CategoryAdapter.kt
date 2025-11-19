package com.example.secondhandmarket.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.ItemDataGenerator
import com.example.secondhandmarket.data.model.ItemCategory

class CategoryAdapter(
    private val context: Context,
    private val categories: List<ItemCategory>,
    private val onCategorySelected: (ItemCategory) -> Unit
) : BaseAdapter() {
    
    private var selectedPosition = -1
    
    override fun getCount(): Int = categories.size
    
    override fun getItem(position: Int): ItemCategory = categories[position]
    
    override fun getItemId(position: Int): Long = position.toLong()
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: CategoryViewHolder
        
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
            holder = CategoryViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = convertView.tag as? CategoryViewHolder ?: CategoryViewHolder(view)
        }
        
        val category = getItem(position)
        val itemCount = getItemCountForCategory(category)
        holder.bind(category, position == selectedPosition, itemCount)
        
        view.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onCategorySelected(category)
        }
        
        return view
    }
    
    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }
    
    private fun getItemCountForCategory(category: ItemCategory): Int {
        return try {
            // 这里可以调用数据管理器获取该分类的商品数量
            // 暂时返回模拟数据
            when (category) {
                ItemCategory.ELECTRONICS -> 15
                ItemCategory.CLOTHING -> 8
                ItemCategory.BOOKS -> 12
                ItemCategory.HOME -> 6
                ItemCategory.SPORTS -> 9
                ItemCategory.BEAUTY -> 7
                ItemCategory.TOYS -> 5
                ItemCategory.DIGITAL -> 11
                ItemCategory.FURNITURE -> 4
                ItemCategory.OTHER -> 3
            }
        } catch (e: Exception) {
            0
        }
    }
    
    private class CategoryViewHolder(private val view: View) {
        private val categoryIcon: ImageView = view.findViewById(R.id.category_icon)
        private val categoryName: TextView = view.findViewById(R.id.category_name)
        private val categoryCount: TextView = view.findViewById(R.id.category_count)
        
        fun bind(category: ItemCategory, isSelected: Boolean, itemCount: Int) {
            categoryName.text = getCategoryDisplayName(category)
            categoryCount.text = "${itemCount}件商品"
            
            // 设置分类图标
            val iconResId = getCategoryIconResId(category)
            categoryIcon.setImageResource(iconResId)
            
            if (isSelected) {
                view.setBackgroundResource(R.drawable.category_selected_bg)
                categoryName.setTextColor(ContextCompat.getColor(categoryName.context, android.R.color.white))
                categoryCount.setTextColor(ContextCompat.getColor(categoryName.context, android.R.color.white))
                categoryIcon.setColorFilter(ContextCompat.getColor(categoryName.context, android.R.color.white))
            } else {
                view.setBackgroundResource(R.drawable.category_normal_bg)
                categoryName.setTextColor(ContextCompat.getColor(categoryName.context, android.R.color.black))
                categoryCount.setTextColor(ContextCompat.getColor(categoryName.context, android.R.color.darker_gray))
                categoryIcon.setColorFilter(ContextCompat.getColor(categoryName.context, android.R.color.darker_gray))
            }
        }
        
        private fun getCategoryDisplayName(category: ItemCategory): String {
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
        
        private fun getCategoryIconResId(category: ItemCategory): Int {
            return when (category) {
                ItemCategory.ELECTRONICS -> R.drawable.ic_electronics
                ItemCategory.CLOTHING -> R.drawable.ic_clothing
                ItemCategory.BOOKS -> R.drawable.ic_books
                ItemCategory.HOME -> R.drawable.ic_home
                ItemCategory.SPORTS -> R.drawable.ic_sports
                ItemCategory.BEAUTY -> R.drawable.ic_beauty
                ItemCategory.TOYS -> R.drawable.ic_toys
                ItemCategory.DIGITAL -> R.drawable.ic_digital
                ItemCategory.FURNITURE -> R.drawable.ic_furniture
                ItemCategory.OTHER -> R.drawable.ic_other
            }
        }
    }
}