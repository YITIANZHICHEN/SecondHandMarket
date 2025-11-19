package com.example.secondhandmarket.data.model

import java.util.Date

data class Order(
    val id: String,
    val itemId: String,
    val itemTitle: String,
    val itemPrice: Double,
    val buyerName: String,
    val sellerName: String,
    var status: OrderStatus,
    val orderDate: Date,
    val quantity: Int = 1,
    val paymentMethod: String = "",
    val shippingAddress: String = "",
    val trackingNumber: String = ""
)

enum class OrderStatus {
    PENDING,      // 待付款
    PAID,         // 已付款
    SHIPPED,      // 已发货
    DELIVERED,    // 已送达
    COMPLETED     // 已完成
}