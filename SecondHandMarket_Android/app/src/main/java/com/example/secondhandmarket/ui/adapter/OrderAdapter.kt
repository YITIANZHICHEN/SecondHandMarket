package com.example.secondhandmarket.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.Order
import com.example.secondhandmarket.data.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val originalOrderList: MutableList<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var currentOrderList = originalOrderList.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(currentOrderList[position])
    }

    override fun getItemCount(): Int = currentOrderList.size

    fun updateOrders(newOrders: List<Order>) {
        currentOrderList.clear()
        currentOrderList.addAll(newOrders)
        notifyDataSetChanged()
    }

    fun filterOrdersByStatus(status: OrderStatus?) {
        currentOrderList = if (status == null) {
            // 显示全部订单
            originalOrderList.toMutableList()
        } else {
            // 根据状态筛选订单
            originalOrderList.filter { it.status == status }.toMutableList()
        }
        notifyDataSetChanged()
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orderIdText: TextView = itemView.findViewById(R.id.order_id_text)
        private val statusChip: com.google.android.material.chip.Chip = itemView.findViewById(R.id.status_chip)
        private val itemImage: ImageView = itemView.findViewById(R.id.item_image)
        private val itemTitleText: TextView = itemView.findViewById(R.id.item_title_text)
        private val sellerBuyerText: TextView = itemView.findViewById(R.id.seller_buyer_text)
        private val priceText: TextView = itemView.findViewById(R.id.price_text)
        private val orderDateText: TextView = itemView.findViewById(R.id.order_date_text)
        private val detailsButton: Button = itemView.findViewById(R.id.details_button)
        private val actionButton: Button = itemView.findViewById(R.id.action_button)

        fun bind(order: Order) {
            // 设置订单号
            orderIdText.text = "订单号：${order.id}"

            // 设置订单状态和颜色
            setStatusChip(order.status)

            // 设置商品信息
            itemTitleText.text = order.itemTitle
            
            // 设置买卖家信息
            val role = if (order.buyerName == "当前用户") "买家" else "卖家"
            sellerBuyerText.text = "$role：${if (order.buyerName == "当前用户") order.buyerName else order.sellerName}"

            // 设置价格
            priceText.text = "￥${String.format("%.2f", order.itemPrice)}"

            // 设置订单日期
            orderDateText.text = "下单时间：${formatDate(order.orderDate)}"

            // 设置商品图片（使用默认占位图）
            itemImage.setImageResource(R.drawable.ic_image_placeholder)

            // 设置按钮点击事件
            detailsButton.setOnClickListener {
                onOrderClick(order)
            }

            actionButton.setOnClickListener {
                onOrderClick(order)
            }

            // 设置主要操作按钮文本
            setActionButtonText(order.status)

            // 设置整个项的点击事件
            itemView.setOnClickListener {
                onOrderClick(order)
            }
        }

        private fun setStatusChip(status: OrderStatus) {
            statusChip.text = getStatusText(status)
            val context = itemView.context
            
            // 根据状态设置颜色
            when (status) {
                OrderStatus.PENDING -> {
                    statusChip.setChipBackgroundColorResource(R.color.warning_light)
                    statusChip.setTextColor(context.getColor(R.color.warning_dark))
                }
                OrderStatus.PAID -> {
                    statusChip.setChipBackgroundColorResource(R.color.primary_light)
                    statusChip.setTextColor(context.getColor(R.color.primary_dark))
                }
                OrderStatus.SHIPPED -> {
                    statusChip.setChipBackgroundColorResource(R.color.info_light)
                    statusChip.setTextColor(context.getColor(R.color.info_dark))
                }
                OrderStatus.DELIVERED -> {
                    statusChip.setChipBackgroundColorResource(R.color.success_light)
                    statusChip.setTextColor(context.getColor(R.color.success_dark))
                }
                OrderStatus.COMPLETED -> {
                    statusChip.setChipBackgroundColorResource(R.color.success)
                    statusChip.setTextColor(context.getColor(R.color.white))
                }
            }
        }

        private fun setActionButtonText(status: OrderStatus) {
            val buttonText = when (status) {
                OrderStatus.PENDING -> "立即支付"
                OrderStatus.PAID -> "查看物流"
                OrderStatus.SHIPPED -> "确认收货"
                OrderStatus.DELIVERED -> "立即评价"
                OrderStatus.COMPLETED -> "查看评价"
            }
            actionButton.text = buttonText
        }

        private fun getStatusText(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "待付款"
                OrderStatus.PAID -> "已付款"
                OrderStatus.SHIPPED -> "已发货"
                OrderStatus.DELIVERED -> "已送达"
                OrderStatus.COMPLETED -> "已完成"
            }
        }

        private fun formatDate(date: Date): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            return formatter.format(date)
        }
    }
}