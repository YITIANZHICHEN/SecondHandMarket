package com.example.secondhandmarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.secondhandmarket.MainActivity
import com.example.secondhandmarket.R
import com.example.secondhandmarket.data.model.User
import java.io.File
import java.io.ObjectInputStream

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private val usersFile by lazy { File(filesDir, "users.dat") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.username_edittext)
        passwordEditText = findViewById(R.id.password_edittext)
        loginButton = findViewById(R.id.login_button)
        registerButton = findViewById(R.id.register_button)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (validateLogin(username, password)) {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                // 登录成功后跳转到主页，并传递用户名信息
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("USERNAME", username)
                // 设置标志以清除登录页面，避免返回
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show()
                // 清空密码输入框
                passwordEditText.text.clear()
            }
        }

        registerButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateLogin(username: String, password: String): Boolean {
        try {
            openFileInput("users.dat").use { fileInput ->
                ObjectInputStream(fileInput).use { objectInput ->
                    val users = objectInput.readObject() as? HashMap<String, User> ?: return false
                    val user = users[username]
                    return user != null && user.password == password
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}