package com.example.secondhandmarket

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "设置"
        
        // 加载设置片段
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsFragment())
                .commit()
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    class SettingsFragment : PreferenceFragmentCompat() {
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            // 获取偏好设置管理器
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
            
            // 设置通知开关监听器
            val notificationPreference = findPreference<SwitchPreferenceCompat>("notifications")
            notificationPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    Toast.makeText(requireContext(), "通知已开启", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "通知已关闭", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // 设置声音开关监听器
            val soundPreference = findPreference<SwitchPreferenceCompat>("sound")
            soundPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    Toast.makeText(requireContext(), "声音已开启", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "声音已关闭", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // 设置振动开关监听器
            val vibrationPreference = findPreference<SwitchPreferenceCompat>("vibration")
            vibrationPreference?.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                if (enabled) {
                    Toast.makeText(requireContext(), "振动已开启", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "振动已关闭", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // 设置主题偏好监听器
            val themePreference = findPreference<androidx.preference.ListPreference>("theme")
            themePreference?.setOnPreferenceChangeListener { _, newValue ->
                val theme = newValue as String
                when (theme) {
                    "light" -> Toast.makeText(requireContext(), "已切换到浅色主题", Toast.LENGTH_SHORT).show()
                    "dark" -> Toast.makeText(requireContext(), "已切换到深色主题", Toast.LENGTH_SHORT).show()
                    "auto" -> Toast.makeText(requireContext(), "已切换到自动主题", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // 设置语言偏好监听器
            val languagePreference = findPreference<androidx.preference.ListPreference>("language")
            languagePreference?.setOnPreferenceChangeListener { _, newValue ->
                val language = newValue as String
                when (language) {
                    "zh" -> Toast.makeText(requireContext(), "已切换到中文", Toast.LENGTH_SHORT).show()
                    "en" -> Toast.makeText(requireContext(), "已切换到英文", Toast.LENGTH_SHORT).show()
                }
                true
            }
            
            // 设置隐私设置点击监听器
            val privacyPreference = findPreference<Preference>("privacy")
            privacyPreference?.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), "隐私设置", Toast.LENGTH_SHORT).show()
                true
            }
            
            // 设置关于点击监听器
            val aboutPreference = findPreference<Preference>("about")
            aboutPreference?.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), "关于应用", Toast.LENGTH_SHORT).show()
                true
            }
            
            // 设置帮助与支持点击监听器
            val helpPreference = findPreference<Preference>("help")
            helpPreference?.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), "帮助与支持", Toast.LENGTH_SHORT).show()
                true
            }
            
            // 设置版本信息点击监听器
            val versionPreference = findPreference<Preference>("version")
            versionPreference?.setOnPreferenceClickListener {
                Toast.makeText(requireContext(), "版本信息", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}