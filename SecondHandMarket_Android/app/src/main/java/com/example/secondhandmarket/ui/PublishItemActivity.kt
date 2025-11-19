package com.example.secondhandmarket.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.ItemCategory

class PublishItemActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var itemTitleEditText: EditText
    private lateinit var itemDescriptionEditText: EditText
    private lateinit var itemPriceEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var conditionSpinner: Spinner
    private lateinit var itemImageView: ImageView
    private lateinit var publishButton: Button
    private var selectedImageUri: Uri? = null

    // 启动图片选择器的结果Launcher
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                itemImageView.setImageURI(uri)
                itemImageView.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_item)

        // 初始化视图
        initViews()
        
        // 设置工具栏
        setupToolbar()
        
        // 设置分类数据
        setupCategorySpinner()
        
        // 设置商品状态数据
        setupConditionSpinner()
        
        // 设置点击事件
        setupClickListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        itemTitleEditText = findViewById(R.id.item_title)
        itemDescriptionEditText = findViewById(R.id.item_description)
        itemPriceEditText = findViewById(R.id.item_price)
        categorySpinner = findViewById(R.id.category_spinner)
        conditionSpinner = findViewById(R.id.condition_spinner)
        itemImageView = findViewById(R.id.item_image)
        publishButton = findViewById(R.id.publish_button)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = "发布商品"
        
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

    private fun setupCategorySpinner() {
        val categories = listOf(
            "请选择分类",
            "电子产品",
            "服装鞋帽", 
            "家居用品",
            "图书文具",
            "运动户外",
            "美妆个护",
            "母婴玩具",
            "其他"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
    }

    private fun setupConditionSpinner() {
        val conditions = listOf(
            "请选择成色",
            "全新",
            "几乎全新",
            "轻微使用痕迹",
            "明显使用痕迹",
            "功能正常"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, conditions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        conditionSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        // 商品图片点击事件
        itemImageView.setOnClickListener {
            selectImage()
        }

        // 提示文字点击事件（当没有选择图片时）
        val imageHintText = findViewById<TextView>(R.id.image_hint_text)
        imageHintText.setOnClickListener {
            selectImage()
        }

        // 发布按钮点击事件
        publishButton.setOnClickListener {
            publishItem()
        }
    }

    private fun selectImage() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "无法打开图片选择器", Toast.LENGTH_SHORT).show()
        }
    }

    private fun publishItem() {
        try {
            // 验证输入
            if (!validateInput()) {
                return
            }

            // 获取输入数据
            val title = itemTitleEditText.text.toString().trim()
            val description = itemDescriptionEditText.text.toString().trim()
            val price = itemPriceEditText.text.toString().toDouble()
            val categoryIndex = categorySpinner.selectedItemPosition
            val conditionIndex = conditionSpinner.selectedItemPosition

            // 根据分类索引映射到ItemCategory枚举
            val category = when (categoryIndex) {
                1 -> ItemCategory.ELECTRONICS
                2 -> ItemCategory.CLOTHING
                3 -> ItemCategory.HOME
                4 -> ItemCategory.BOOKS
                5 -> ItemCategory.SPORTS
                6 -> ItemCategory.BEAUTY
                7 -> ItemCategory.TOYS
                8 -> ItemCategory.OTHER
                else -> ItemCategory.OTHER
            }

            // 模拟发布成功（实际应用中应该上传到服务器）
            android.util.Log.d("PublishItemActivity", "发布商品: $title")
            android.util.Log.d("PublishItemActivity", "价格: $price")
            android.util.Log.d("PublishItemActivity", "分类: $category")
            android.util.Log.d("PublishItemActivity", "描述: $description")

            // 显示成功消息
            Toast.makeText(this, "商品发布成功！", Toast.LENGTH_SHORT).show()

            // 延迟关闭页面，返回个人中心
            android.os.Handler(mainLooper).postDelayed({
                setResult(RESULT_OK)
                finish()
            }, 1500)

        } catch (e: Exception) {
            android.util.Log.e("PublishItemActivity", "发布商品失败", e)
            Toast.makeText(this, "发布失败：${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInput(): Boolean {
        // 验证标题
        val title = itemTitleEditText.text.toString().trim()
        if (title.isEmpty()) {
            itemTitleEditText.error = "请输入商品标题"
            itemTitleEditText.requestFocus()
            return false
        }

        // 验证描述
        val description = itemDescriptionEditText.text.toString().trim()
        if (description.isEmpty()) {
            itemDescriptionEditText.error = "请输入商品描述"
            itemDescriptionEditText.requestFocus()
            return false
        }

        // 验证价格
        val priceStr = itemPriceEditText.text.toString().trim()
        if (priceStr.isEmpty()) {
            itemPriceEditText.error = "请输入商品价格"
            itemPriceEditText.requestFocus()
            return false
        }

        try {
            val price = priceStr.toDouble()
            if (price <= 0) {
                itemPriceEditText.error = "价格必须大于0"
                itemPriceEditText.requestFocus()
                return false
            }
        } catch (e: NumberFormatException) {
            itemPriceEditText.error = "请输入有效的价格"
            itemPriceEditText.requestFocus()
            return false
        }

        // 验证分类
        if (categorySpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "请选择商品分类", Toast.LENGTH_SHORT).show()
            categorySpinner.requestFocus()
            return false
        }

        // 验证成色
        if (conditionSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "请选择商品成色", Toast.LENGTH_SHORT).show()
            conditionSpinner.requestFocus()
            return false
        }

        return true
    }

    // 重写onResume来刷新数据（如果用户从其他页面返回）
    override fun onResume() {
        super.onResume()
        // 可以在这里重新加载用户数据或其他必要操作
    }
}