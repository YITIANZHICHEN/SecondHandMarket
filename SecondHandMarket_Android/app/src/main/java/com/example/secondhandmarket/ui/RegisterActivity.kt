package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.User
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.HashMap

class RegisterActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var backToLoginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameEditText = findViewById(R.id.username_edittext)
        passwordEditText = findViewById(R.id.password_edittext)
        confirmPasswordEditText = findViewById(R.id.confirm_password_edittext)
        registerButton = findViewById(R.id.register_button)
        backToLoginButton = findViewById(R.id.back_to_login_button)

        registerButton.setOnClickListener {
            registerUser()
        }

        backToLoginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val username = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
            return
        }

        // 验证用户名是否已存在
        val users = loadUsers()
        if (users.containsKey(username)) {
            Toast.makeText(this, "用户名已存在", Toast.LENGTH_SHORT).show()
            return
        }

        // 创建新用户
        val newUser = User(username = username, password = password)
        users[username] = newUser
        saveUsers(users)

        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
        // 注册成功后跳转到登录页面
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun loadUsers(): HashMap<String, User> {
        return try {
            openFileInput("users.dat").use { inputStream ->
                ObjectInputStream(inputStream).use { objectInputStream ->
                    objectInputStream.readObject() as? HashMap<String, User> ?: HashMap()
                }
            }
        } catch (e: Exception) {
            HashMap()
        }
    }

    private fun saveUsers(users: HashMap<String, User>) {
        try {
            openFileOutput("users.dat", MODE_PRIVATE).use { outputStream ->
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(users)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "保存用户信息失败", Toast.LENGTH_SHORT).show()
        }
    }
}