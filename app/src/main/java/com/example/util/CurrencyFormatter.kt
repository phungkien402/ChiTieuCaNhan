package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val formatter = DecimalFormat("#,###", symbols)

    fun formatVnd(amount: Double): String {
        return "${formatter.format(amount)} đ"
    }

    fun parseAmount(text: String): Double {
        val clean = text.replace(".", "").replace(",", "").replace("đ", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun formatDate(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN"))
        return sdf.format(timestamp.toDate())
    }

    fun formatDateOnly(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN"))
        return sdf.format(timestamp.toDate())
    }

    fun formatMonthYear(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""
        val sdf = SimpleDateFormat("MM/yyyy", Locale("vi", "VN"))
        return sdf.format(timestamp.toDate())
    }
}
