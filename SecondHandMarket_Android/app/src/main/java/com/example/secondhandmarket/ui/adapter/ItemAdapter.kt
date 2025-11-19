package com.example.secondhandmarket.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.Item
import com.example.secondhandmarket.data.model.ItemStatus

class ItemAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit,
    private val onFavoriteClick: (Item, Int) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        val itemTitle: TextView = itemView.findViewById(R.id.item_title)
        val itemPrice: TextView = itemView.findViewById(R.id.item_price)
        val itemLocation: TextView = itemView.findViewById(R.id.item_location)
        val itemStatus: TextView = itemView.findViewById(R.id.item_status)
        val itemBrand: TextView = itemView.findViewById(R.id.item_brand)
        val itemCondition: TextView = itemView.findViewById(R.id.item_condition)
        val itemDescription: TextView = itemView.findViewById(R.id.item_description)
        val favoriteButton: ImageView = itemView.findViewById(R.id.btn_favorite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        try {
            val item = items.getOrNull(position) ?: return
            
            // 根据商品分类设置对应的本地图标
            try {
                val imageResId = when (item.category) {
                    com.example.secondhandmarket.data.model.ItemCategory.ELECTRONICS -> R.drawable.ic_electronics
                    com.example.secondhandmarket.data.model.ItemCategory.CLOTHING -> R.drawable.button_rounded
                    com.example.secondhandmarket.data.model.ItemCategory.BOOKS -> R.drawable.category_normal_bg
                    com.example.secondhandmarket.data.model.ItemCategory.HOME -> R.drawable.brand_bg
                    com.example.secondhandmarket.data.model.ItemCategory.SPORTS -> R.drawable.condition_bg
                    com.example.secondhandmarket.data.model.ItemCategory.BEAUTY -> R.drawable.button_border
                    com.example.secondhandmarket.data.model.ItemCategory.TOYS -> R.drawable.category_selected_bg
                    com.example.secondhandmarket.data.model.ItemCategory.DIGITAL -> R.drawable.ic_launcher_foreground
                    com.example.secondhandmarket.data.model.ItemCategory.FURNITURE -> R.drawable.button_red_rounded
                    com.example.secondhandmarket.data.model.ItemCategory.OTHER -> R.drawable.ic_launcher_background
                    else -> R.drawable.ic_launcher_background
                }
                
                // 设置图片（将矢量图标作为背景，同时显示商品分类的首字母）
                holder.itemImage.setImageResource(imageResId)
                
            } catch (e: Exception) {
                // 如果设置分类图标失败，使用默认图标
                holder.itemImage.setImageResource(R.drawable.ic_launcher_background)
            }
            
            // 设置商品信息（添加空值检查）
            holder.itemTitle.text = item.title ?: ""
            holder.itemPrice.text = "￥${item.price ?: 0.0}"
            holder.itemLocation.text = item.location ?: ""
            
            // 设置品牌信息
            holder.itemBrand.text = item.brand ?: "未知品牌"
            
            // 设置成色信息
            holder.itemCondition.text = when (item.condition) {
                com.example.secondhandmarket.data.model.ItemCondition.NEW -> "全新"
                com.example.secondhandmarket.data.model.ItemCondition.LIKE_NEW -> "几乎全新"
                com.example.secondhandmarket.data.model.ItemCondition.GOOD -> "良好"
                com.example.secondhandmarket.data.model.ItemCondition.FAIR -> "一般"
                com.example.secondhandmarket.data.model.ItemCondition.POOR -> "较差"
                else -> "未知"
            }
            
            // 设置描述信息
            holder.itemDescription.text = item.description ?: ""
            
            // 设置分类信息（在位置后面显示）
            val categoryName = when (item.category) {
                com.example.secondhandmarket.data.model.ItemCategory.ELECTRONICS -> "电子产品"
                com.example.secondhandmarket.data.model.ItemCategory.CLOTHING -> "服装鞋帽"
                com.example.secondhandmarket.data.model.ItemCategory.BOOKS -> "图书文具"
                com.example.secondhandmarket.data.model.ItemCategory.HOME -> "家居用品"
                com.example.secondhandmarket.data.model.ItemCategory.SPORTS -> "运动户外"
                com.example.secondhandmarket.data.model.ItemCategory.BEAUTY -> "美妆个护"
                com.example.secondhandmarket.data.model.ItemCategory.TOYS -> "玩具游戏"
                com.example.secondhandmarket.data.model.ItemCategory.DIGITAL -> "数码配件"
                com.example.secondhandmarket.data.model.ItemCategory.FURNITURE -> "家具家电"
                com.example.secondhandmarket.data.model.ItemCategory.OTHER -> "其他"
                else -> "未知分类"
            }
            holder.itemLocation.text = "${item.location ?: ""} · $categoryName"
            
            // 设置商品状态
            try {
                when (item.status) {
                    ItemStatus.AVAILABLE -> {
                        holder.itemStatus.text = "可购买"
                        holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
                    }
                    ItemStatus.SOLD -> {
                        holder.itemStatus.text = "已售出"
                        holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
                    }
                    ItemStatus.RESERVED -> {
                        holder.itemStatus.text = "已预订"
                        holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_orange_dark))
                    }
                    else -> {
                        holder.itemStatus.text = "未知状态"
                        holder.itemStatus.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
                    }
                }
            } catch (e: Exception) {
                holder.itemStatus.text = "状态未知"
            }
            
            // 设置收藏按钮状态和点击事件
            try {
                // 根据收藏状态设置按钮图标
                if (item.isFavorite) {
                    holder.favoriteButton.setImageResource(R.drawable.ic_favorite_filled)
                } else {
                    holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
                }
                
                // 收藏按钮点击事件
                holder.favoriteButton.setOnClickListener { view ->
                    try {
                        onFavoriteClick(item, holder.adapterPosition)
                    } catch (e: Exception) {
                        // 忽略收藏按钮点击事件异常
                    }
                }
            } catch (e: Exception) {
                // 如果设置收藏按钮失败，使用默认状态
                holder.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
            }
            
            // 设置整个item的点击事件
            holder.itemView.setOnClickListener {
                try {
                    onItemClick(item)
                } catch (e: Exception) {
                    // 忽略点击事件异常
                }
            }
        } catch (e: Exception) {
            // 忽略整个绑定过程中的异常，避免崩溃
        }
    }

    override fun getItemCount(): Int = items.size

    // 更新数据集的方法
    fun updateItems(newItems: List<Item>) {
        try {
            this.items = newItems ?: emptyList()
            notifyDataSetChanged()
        } catch (e: Exception) {
            // 忽略更新数据集中的异常，避免崩溃
        }
    }
    
    /**
     * 更新单个Item的收藏状态
     */
    fun updateFavoriteStatus(itemId: Long, isFavorite: Boolean) {
        try {
            val currentItems = this.items.toMutableList()
            val itemIndex = currentItems.indexOfFirst { it.id == itemId }
            if (itemIndex != -1) {
                val updatedItem = currentItems[itemIndex].copy(isFavorite = isFavorite)
                currentItems[itemIndex] = updatedItem
                this.items = currentItems
                notifyItemChanged(itemIndex)
            }
        } catch (e: Exception) {
            // 忽略更新收藏状态的异常
        }
    }
}