package com.example.model

data class UserSettings(
    val monthlyBudget: Double = 5000000.0, // Mặc định 5 triệu VNĐ
    val reminderHour: Int = 21,
    val reminderMinute: Int = 0,
    val isReminderEnabled: Boolean = true,
    val isDarkMode: Boolean? = null // null: Theo hệ thống, true: Dark, false: Light
)
