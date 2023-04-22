package com.example.tippee_atm_mobile_version

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Balance : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balance)

        //Date & Time
        var time = findViewById<TextView>(R.id.time)
        var date = findViewById<TextView>(R.id.date)
        val dateTime = DateTime()
        time.text=dateTime.getTimeString()
        date.text= dateTime.getDateString()


        val userAccountNumber = intent.getStringExtra("UserAccountNumber")
    }
}