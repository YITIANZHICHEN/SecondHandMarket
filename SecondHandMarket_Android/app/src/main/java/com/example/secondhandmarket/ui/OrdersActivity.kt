package com.example.secondhandmarket.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.Order
import com.example.secondhandmarket.data.model.OrderStatus
import com.example.secondhandmarket.ui.adapter.OrderAdapter
import java.util.Date

class OrdersActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var ordersRecyclerView: RecyclerView
    private lateinit var orderStatusTabs: TabLayout
    private lateinit var emptyView: View
    private lateinit var emptyText: TextView
    
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var orderList: MutableList<Order>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        // 初始化视图
        initViews()
        
        // 设置工具栏
        setupToolbar()
        
        // 初始化订单数据
        initOrderData()
        
        // 设置订单状态标签栏
        setupOrderStatusTabs()
        
        // 设置RecyclerView
        setupRecyclerView()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ordersRecyclerView = findViewById(R.id.orders_recycler_view)
        orderStatusTabs = findViewById(R.id.order_status_tabs)
        emptyView = findViewById(R.id.empty_view)
        emptyText = findViewById(R.id.empty_text)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "我的订单"
        
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
    }

    private fun initOrderData() {
        // 创建模拟订单数据
        orderList = mutableListOf(
            Order(
                id = "ORD001",
                itemId = "ITEM001",
                itemTitle = "iPhone 13 Pro Max",
                itemPrice = 6999.0,
                buyerName = "张三",
                sellerName = "李四",
                status = OrderStatus.PAID,
                orderDate = Date(),
                quantity = 1
            ),
            Order(
                id = "ORD002", 
                itemId = "ITEM002",
                itemTitle = "MacBook Air M1",
                itemPrice = 7999.0,
                buyerName = "王五",
                sellerName = "当前用户",
                status = OrderStatus.SHIPPED,
                orderDate = Date(),
                quantity = 1
            ),
            Order(
                id = "ORD003",
                itemId = "ITEM003", 
                itemTitle = "Nike运动鞋",
                itemPrice = 299.0,
                buyerName = "赵六",
                sellerName = "孙七",
                status = OrderStatus.PENDING,
                orderDate = Date(),
                quantity = 1
            ),
            Order(
                id = "ORD004",
                itemId = "ITEM004",
                itemTitle = "索尼耳机",
                itemPrice = 1299.0,
                buyerName = "当前用户", 
                sellerName = "周八",
                status = OrderStatus.DELIVERED,
                orderDate = Date(),
                quantity = 1
            ),
            Order(
                id = "ORD005",
                itemId = "ITEM005",
                itemTitle = "Switch游戏机",
                itemPrice = 1899.0,
                buyerName = "吴九",
                sellerName = "当前用户",
                status = OrderStatus.COMPLETED,
                orderDate = Date(),
                quantity = 1
            )
        )
    }

    private fun setupRecyclerView() {
        try {
            // 创建适配器
            orderAdapter = OrderAdapter(orderList) { order ->
                // 订单项点击事件
                onOrderItemClick(order)
            }
            
            // 设置布局管理器
            ordersRecyclerView.layoutManager = LinearLayoutManager(this)
            
            // 设置适配器
            ordersRecyclerView.adapter = orderAdapter
            
            // 显示数据或空视图
            updateEmptyView()
            
            android.util.Log.d("OrdersActivity", "RecyclerView设置完成，订单数量: ${orderList.size}")
            
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "设置RecyclerView失败", e)
        }
    }

    private fun updateEmptyView() {
        if (orderList.isEmpty()) {
            ordersRecyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            emptyText.text = "暂无订单记录"
        } else {
            ordersRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }
    }
    
    /**
     * 设置订单状态标签栏
     */
    private fun setupOrderStatusTabs() {
        // 添加标签选择事件监听器
        orderStatusTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // 根据选中的标签筛选订单
                when (tab?.position) {
                    0 -> filterOrders(null)  // 全部
                    1 -> filterOrders(OrderStatus.PENDING)  // 待付款
                    2 -> filterOrders(OrderStatus.PAID)  // 待发货（已付款待发货）
                    3 -> filterOrders(OrderStatus.SHIPPED)  // 待收货（已发货）
                    4 -> filterOrders(OrderStatus.DELIVERED)  // 待评价（已送达）
                    5 -> filterOrders(OrderStatus.COMPLETED)  // 已完成
                }
            }
            
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // 不需要处理
            }
            
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // 不需要处理
            }
        })
        
        // 默认选择"全部"标签
        orderStatusTabs.getTabAt(0)?.select()
    }
    
    /**
     * 筛选订单
     */
    private fun filterOrders(status: OrderStatus?) {
        try {
            orderAdapter.filterOrdersByStatus(status)
            
            // 更新空状态显示
            val count = orderAdapter.itemCount
            if (count == 0) {
                emptyView.visibility = View.VISIBLE
                ordersRecyclerView.visibility = View.GONE
                
                // 根据筛选状态显示不同的空状态文本
                val statusText = when (status) {
                    OrderStatus.PENDING -> "待付款"
                    OrderStatus.PAID -> "待发货"
                    OrderStatus.SHIPPED -> "待收货"
                    OrderStatus.DELIVERED -> "待评价"
                    OrderStatus.COMPLETED -> "已完成"
                    else -> "订单"
                }
                
                emptyText.text = "暂无${statusText}记录"
            } else {
                emptyView.visibility = View.GONE
                ordersRecyclerView.visibility = View.VISIBLE
            }
            
            android.util.Log.d("OrdersActivity", "筛选订单状态: $status, 剩余订单数: $count")
            
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "筛选订单失败", e)
        }
    }

    private fun onOrderItemClick(order: Order) {
        try {
            android.util.Log.d("OrdersActivity", "点击订单: ${order.id}")
            
            // 根据订单状态执行不同操作
            when (order.status) {
                OrderStatus.PENDING -> {
                    // 待付款订单，显示支付选项
                    showPaymentOptions(order)
                }
                OrderStatus.PAID -> {
                    // 已付款订单，显示物流跟踪
                    showShippingTracking(order)
                }
                OrderStatus.SHIPPED -> {
                    // 已发货订单，显示确认收货
                    showConfirmReceipt(order)
                }
                OrderStatus.DELIVERED -> {
                    // 已送达订单，显示评价选项
                    showReviewOptions(order)
                }
                OrderStatus.COMPLETED -> {
                    // 已完成订单，显示已完成
                    showOrderCompleted(order)
                }
            }
            
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "订单项点击事件失败", e)
        }
    }

    private fun showPaymentOptions(order: Order) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("支付订单")
        builder.setMessage("订单号: ${order.id}\n商品: ${order.itemTitle}\n金额: ￥${order.itemPrice}")
        builder.setPositiveButton("立即支付") { _, _ ->
            performPayment(order)
        }
        builder.setNegativeButton("取消", null)
        builder.setNeutralButton("查看详情") { _, _ ->
            showOrderDetails(order)
        }
        builder.show()
    }



    private fun showConfirmReceipt(order: Order) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("确认收货")
        builder.setMessage("订单号: ${order.id}\n商品: ${order.itemTitle}\n您确认已收到商品吗？")
        builder.setPositiveButton("确认收货") { _, _ ->
            confirmReceipt(order)
        }
        builder.setNegativeButton("物流查询") { _, _ ->
            checkShippingInfo(order)
        }
        builder.show()
    }

    private fun showReviewOptions(order: Order) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("评价商品")
        builder.setMessage("订单号: ${order.id}\n商品: ${order.itemTitle}\n请为本次交易评价")
        builder.setPositiveButton("好评") { _, _ ->
            submitReview(order, 5)
        }
        builder.setNegativeButton("中评") { _, _ ->
            submitReview(order, 3)
        }
        builder.setNeutralButton("差评") { _, _ ->
            submitReview(order, 1)
        }
        builder.show()
    }

    private fun showOrderCompleted(order: Order) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("订单已完成")
        builder.setMessage("订单号: ${order.id}\n商品: ${order.itemTitle}\n感谢您的购买！")
        builder.setPositiveButton("确定", null)
        builder.setNeutralButton("再次购买") { _, _ ->
            repurchase(order)
        }
        builder.show()
    }

    // 各种操作的具体实现
    private fun performPayment(order: Order) {
        try {
            // 模拟支付成功
            order.status = OrderStatus.PAID
            orderAdapter.notifyDataSetChanged()
            updateEmptyView()
            
            android.util.Log.d("OrdersActivity", "订单支付成功: ${order.id}")
            
            // 显示成功消息
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("支付成功")
                .setMessage("订单支付成功！")
                .setPositiveButton("确定", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "支付失败", e)
        }
    }

    private fun showOrderDetails(order: Order) {
        // 显示订单详情（可以是跳转到一个新页面）
        android.util.Log.d("OrdersActivity", "查看订单详情: ${order.id}")
    }

    private fun contactSeller(order: Order) {
        android.util.Log.d("OrdersActivity", "联系卖家: ${order.sellerName}")
        // 实际应用中应该是打开聊天界面或拨打电话
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("联系卖家")
            .setMessage("正在开发联系卖家功能...")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun confirmReceipt(order: Order) {
        try {
            order.status = OrderStatus.DELIVERED
            orderAdapter.notifyDataSetChanged()
            
            android.util.Log.d("OrdersActivity", "确认收货: ${order.id}")
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("收货确认")
                .setMessage("已确认收货，感谢您的购买！")
                .setPositiveButton("确定", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "确认收货失败", e)
        }
    }

    private fun checkShippingInfo(order: Order) {
        android.util.Log.d("OrdersActivity", "查看物流信息: ${order.id}")
        // 实际应用中应该打开物流跟踪页面
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("物流跟踪")
            .setMessage("正在开发物流跟踪功能...")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun submitReview(order: Order, rating: Int) {
        try {
            order.status = OrderStatus.COMPLETED
            orderAdapter.notifyDataSetChanged()
            
            android.util.Log.d("OrdersActivity", "提交评价: ${order.id}, 评分: $rating")
            
            val ratingText = when (rating) {
                5 -> "好评"
                3 -> "中评"
                1 -> "差评"
                else -> "评价"
            }
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("评价成功")
                .setMessage("感谢您的$ratingText！")
                .setPositiveButton("确定", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "提交评价失败", e)
        }
    }

    private fun showShippingTracking(order: Order) {
        try {
            android.util.Log.d("OrdersActivity", "显示物流跟踪: ${order.id}")
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("物流跟踪")
                .setMessage("订单号: ${order.id}\n商品: ${order.itemTitle}\n状态: 卖家已发货\n快递公司: 顺丰速运\n快递单号: SF123456789")
                .setPositiveButton("确定", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "显示物流跟踪失败", e)
        }
    }

    private fun repurchase(order: Order) {
        try {
            android.util.Log.d("OrdersActivity", "再次购买: ${order.itemId}")
            // 实际应用中应该跳转到商品详情页或添加到购物车
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("再次购买")
                .setMessage("正在添加商品到购物车...")
                .setPositiveButton("确定", null)
                .show()
                
        } catch (e: Exception) {
            android.util.Log.e("OrdersActivity", "再次购买失败", e)
        }
    }
}