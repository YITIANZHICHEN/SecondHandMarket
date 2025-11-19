package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.secondhandmarket.R
import com.example.secondhandmarket.UserManager
import com.example.secondhandmarket.data.model.User

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_EDIT_PROFILE = 1001
    }

    private lateinit var toolbar: Toolbar
    private lateinit var usernameTextView: TextView
    private lateinit var userIdTextView: TextView
    private lateinit var userStatusTextView: TextView
    private lateinit var editProfileBtn: ImageButton
    private lateinit var publishedCountText: TextView
    private lateinit var orderCountText: TextView
    private lateinit var publishItemBtn: View
    private lateinit var myOrdersBtn: View
    private lateinit var myFavoritesBtn: View
    private lateinit var settingsBtn: View
    private lateinit var logoutBtn: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 初始化视图
        initViews()
        
        // 设置工具栏
        setupToolbar()
        
        // 加载用户数据
        loadUserData()
        
        // 设置点击事件
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        usernameTextView = findViewById(R.id.username_text)
        userIdTextView = findViewById(R.id.user_id_text)
        userStatusTextView = findViewById(R.id.user_status_text)
        editProfileBtn = findViewById(R.id.edit_profile_btn)
        publishedCountText = findViewById(R.id.published_count)
        orderCountText = findViewById(R.id.order_count)
        publishItemBtn = findViewById(R.id.publish_item_btn)
        myOrdersBtn = findViewById(R.id.my_orders_btn)
        myFavoritesBtn = findViewById(R.id.my_favorites_btn)
        settingsBtn = findViewById(R.id.settings_btn)
        logoutBtn = findViewById(R.id.logout_button)
    }

    private fun setupToolbar() {
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
    }

    private fun loadUserData() {
        // 从UserManager获取当前用户数据
        val userManager = UserManager.getInstance(this)
        val currentUser = userManager.getCurrentUser()
        
        if (currentUser != null) {
            // 使用真实用户数据
            usernameTextView.text = currentUser.getDisplayName()
            userIdTextView.text = "ID: ${currentUser.username.hashCode()}"
            
            // 设置用户状态
            userStatusTextView.text = "已认证用户"
            
            // 如果有昵称，显示昵称
            if (!currentUser.nickname.isNullOrEmpty()) {
                usernameTextView.text = currentUser.nickname
            }
        } else {
            // 如果没有用户数据，使用默认值
            val username = intent.getStringExtra("USERNAME")
            if (!username.isNullOrEmpty()) {
                usernameTextView.text = username
            } else {
                usernameTextView.text = "用户名"
            }
            userIdTextView.text = "ID: 123456789"
            userStatusTextView.text = "已认证用户"
        }
        
        // 模拟统计数据（实际应用中应该从服务器获取）
        publishedCountText.text = "12"
        orderCountText.text = "5"
    }

    private fun setupClickListeners() {
        // 编辑个人资料按钮
        editProfileBtn.setOnClickListener {
            try {
                android.util.Log.d("ProfileActivity", "开始跳转到编辑个人资料页面")
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE)
                android.util.Log.d("ProfileActivity", "已跳转到编辑个人资料页面")
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "跳转到编辑个人资料页面失败", e)
                Toast.makeText(this, "无法打开编辑个人资料页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 发布商品按钮
        publishItemBtn.setOnClickListener {
            try {
                android.util.Log.d("ProfileActivity", "开始跳转到发布商品页面")
                val intent = Intent(this, PublishItemActivity::class.java)
                startActivity(intent)
                android.util.Log.d("ProfileActivity", "已跳转到发布商品页面")
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "跳转到发布商品页面失败", e)
                Toast.makeText(this, "无法打开发布商品页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 我的订单按钮
        myOrdersBtn.setOnClickListener {
            try {
                android.util.Log.d("ProfileActivity", "开始跳转到我的订单页面")
                val intent = Intent(this, OrdersActivity::class.java)
                startActivity(intent)
                android.util.Log.d("ProfileActivity", "已跳转到我的订单页面")
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "跳转到我的订单页面失败", e)
                Toast.makeText(this, "无法打开我的订单页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 我的收藏按钮
        myFavoritesBtn.setOnClickListener {
            try {
                android.util.Log.d("ProfileActivity", "开始跳转到收藏页面")
                val intent = Intent(this, FavoritesActivity::class.java)
                startActivity(intent)
                android.util.Log.d("ProfileActivity", "已跳转到收藏页面")
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "跳转到收藏页面失败", e)
                Toast.makeText(this, "无法打开收藏页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置按钮
        settingsBtn.setOnClickListener {
            try {
                android.util.Log.d("ProfileActivity", "开始跳转到设置页面")
                val intent = Intent(this, com.example.secondhandmarket.SettingsActivity::class.java)
                startActivity(intent)
                android.util.Log.d("ProfileActivity", "已跳转到设置页面")
            } catch (e: Exception) {
                android.util.Log.e("ProfileActivity", "跳转到设置页面失败", e)
                Toast.makeText(this, "无法打开设置页面", Toast.LENGTH_SHORT).show()
            }
        }

        // 退出登录按钮
        logoutBtn.setOnClickListener {
            // 显示确认对话框
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("确认退出")
        builder.setMessage("您确定要退出登录吗？")
        builder.setPositiveButton("确定") { _, _ ->
            performLogout()
        }
        builder.setNegativeButton("取消", null)
        builder.show()
    }

    private fun performLogout() {
        try {
            // 清除用户数据（实际应用中应该清除本地存储的用户信息）
            // SharedPreferences etc.
            
            // 跳转到登录页面
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // 结束所有Activity
            finishAffinity()
            
            Toast.makeText(this, "已成功退出登录", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "退出登录失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 处理编辑个人资料后的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_CODE_EDIT_PROFILE -> {
                if (resultCode == RESULT_OK) {
                    // 重新加载用户数据
                    loadUserData()
                    Toast.makeText(this, "个人资料已更新", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 重写onResume来刷新数据（如果用户从其他页面返回）
    override fun onResume() {
        super.onResume()
        // 可以在这里重新加载用户数据，如统计数据等
        loadUserData()
    }
}