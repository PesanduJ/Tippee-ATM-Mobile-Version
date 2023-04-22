package com.example.tippee_atm_mobile_version

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateTime {

    fun getTimeString(): String {
        val currentTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(currentTime)
    }

    fun getDateString(): String {
        val dateFormat = SimpleDateFormat("EEEE, d'${getDayOfMonthSuffix(Calendar.getInstance().get(Calendar.DAY_OF_MONTH))}' MMMM yyyy", Locale.ENGLISH)
        val currentDate = Date()
        val formattedDate = dateFormat.format(currentDate)
        return formattedDate
    }

    fun getDayOfMonthSuffix(day: Int): String {
        if (day in 11..13) {
            return "th"
        }
        return when (day % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}