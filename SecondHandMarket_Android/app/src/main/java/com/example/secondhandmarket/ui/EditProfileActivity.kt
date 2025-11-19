package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.secondhandmarket.R
import com.example.secondhandmarket.UserManager
import com.example.secondhandmarket.data.model.User

class EditProfileActivity : AppCompatActivity() {
    
    private lateinit var toolbar: Toolbar
    private lateinit var avatarImageView: ImageView
    private lateinit var nicknameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthdayEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    
    private var currentUser: User? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        initViews()
        setupToolbar()
        setupGenderSpinner()
        loadUserData()
        setupClickListeners()
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        avatarImageView = findViewById(R.id.avatar_image_view)
        nicknameEditText = findViewById(R.id.nickname_edit_text)
        usernameEditText = findViewById(R.id.username_edit_text)
        emailEditText = findViewById(R.id.email_edit_text)
        phoneEditText = findViewById(R.id.phone_edit_text)
        bioEditText = findViewById(R.id.bio_edit_text)
        locationEditText = findViewById(R.id.location_edit_text)
        genderSpinner = findViewById(R.id.gender_spinner)
        birthdayEditText = findViewById(R.id.birthday_edit_text)
        saveButton = findViewById(R.id.save_button)
        cancelButton = findViewById(R.id.cancel_button)
    }
    
    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "编辑个人资料"
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupGenderSpinner() {
        val genders = arrayOf("请选择", "男", "女", "其他")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }
    
    private fun loadUserData() {
        currentUser = UserManager.getInstance(this).getCurrentUser()
        
        currentUser?.let { user ->
            // 设置头像（这里使用默认头像，实际应用中可以从网络或本地加载）
            avatarImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            
            nicknameEditText.setText(user.nickname ?: "")
            usernameEditText.setText(user.username)
            emailEditText.setText(user.email ?: "")
            phoneEditText.setText(user.phone ?: "")
            bioEditText.setText(user.bio ?: "")
            locationEditText.setText(user.location ?: "")
            birthdayEditText.setText(user.birthday ?: "")
            
            // 设置性别选择
            user.gender?.let { gender ->
                val genders = arrayOf("请选择", "男", "女", "其他")
                val position = genders.indexOf(gender)
                if (position != -1) {
                    genderSpinner.setSelection(position)
                }
            }
        }
        
        // 用户名不可编辑
        usernameEditText.isEnabled = false
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveProfile()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
        
        // 头像点击事件（实际应用中可以实现头像上传功能）
        avatarImageView.setOnClickListener {
            showAvatarSelectionDialog()
        }
        
        // 生日选择器
        birthdayEditText.setOnClickListener {
            showDatePickerDialog()
        }
    }
    
    private fun saveProfile() {
        val nickname = nicknameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()
        val location = locationEditText.text.toString().trim()
        val gender = if (genderSpinner.selectedItemPosition > 0) {
            genderSpinner.selectedItem.toString()
        } else {
            null
        }
        val birthday = birthdayEditText.text.toString().trim()
        
        // 基本验证
        if (email.isNotEmpty() && !isValidEmail(email)) {
            Toast.makeText(this, "请输入有效的邮箱地址", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (phone.isNotEmpty() && !isValidPhone(phone)) {
            Toast.makeText(this, "请输入有效的手机号码", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 更新用户信息
        currentUser?.let { user ->
            val updatedUser = user.copy(
                nickname = if (nickname.isNotEmpty()) nickname else null,
                email = if (email.isNotEmpty()) email else null,
                phone = if (phone.isNotEmpty()) phone else null,
                bio = if (bio.isNotEmpty()) bio else null,
                location = if (location.isNotEmpty()) location else null,
                gender = gender,
                birthday = if (birthday.isNotEmpty()) birthday else null
            )
            
            // 保存到UserManager
            UserManager.getInstance(this).saveCurrentUser(updatedUser)
            
            Toast.makeText(this, "个人资料保存成功", Toast.LENGTH_SHORT).show()
            
            // 返回结果
            val resultIntent = Intent()
            resultIntent.putExtra("user_updated", true)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhone(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }
    
    private fun showAvatarSelectionDialog() {
        // 实际应用中可以实现头像选择功能
        Toast.makeText(this, "头像选择功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDatePickerDialog() {
        // 实际应用中可以实现日期选择器
        Toast.makeText(this, "日期选择功能待实现", Toast.LENGTH_SHORT).show()
    }
}