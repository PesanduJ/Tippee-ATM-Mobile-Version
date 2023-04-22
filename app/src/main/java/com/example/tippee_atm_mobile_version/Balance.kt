package com.example.tippee_atm_mobile_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

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

        var accountNumber = findViewById<TextView>(R.id.accountNumber)
        var accountName = findViewById<TextView>(R.id.accountName)
        var balance = findViewById<TextView>(R.id.balance)
        var back= findViewById<Button>(R.id.back)


        val userAccountNumber = intent.getStringExtra("UserAccountNumber")

        val dbHelper = MinutiaeDatabaseHelper(applicationContext)

        val userData = userAccountNumber?.let { dbHelper.getUserDataByAccountNo(it.toInt()) }

        if (userData != null) {
            accountNumber.text = userAccountNumber.toString()
            accountName.text = userData.first
            balance.text = userData.second.first.toString()
        } else {
            Toast.makeText(applicationContext, "Could not retrieve user data!", Toast.LENGTH_SHORT).show()
        }

        back.setOnClickListener {
            var myintent = Intent(applicationContext, Dashboard::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }
    }
}