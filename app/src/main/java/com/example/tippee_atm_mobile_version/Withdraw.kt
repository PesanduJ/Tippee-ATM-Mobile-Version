package com.example.tippee_atm_mobile_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Withdraw : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw)

        //Date & Time
        var time = findViewById<TextView>(R.id.time)
        var date = findViewById<TextView>(R.id.date)
        val dateTime = DateTime()
        time.text=dateTime.getTimeString()
        date.text= dateTime.getDateString()

        var amountToDeduct = findViewById<EditText>(R.id.amount)
        var withdrawMoney = findViewById<Button>(R.id.withdrawMoney)
        var cancel = findViewById<Button>(R.id.cancel)

        val userAccountNumber = intent.getStringExtra("UserAccountNumber")

        withdrawMoney.setOnClickListener {
            val dbHelper = MinutiaeDatabaseHelper(applicationContext)
            val success = userAccountNumber?.let { dbHelper.deductAmountByAccountNo(it.toInt(), amountToDeduct.text.toString().toInt()) }
            if (success == true) {
                Toast.makeText(applicationContext, "Withdrawal successful!", Toast.LENGTH_SHORT).show()
                var myintent = Intent(applicationContext, Dashboard::class.java)
                myintent.putExtra("UserAccountNumber",userAccountNumber)
                startActivity(myintent)
            } else {
                Toast.makeText(applicationContext, "Insufficient balance!", Toast.LENGTH_SHORT).show()
            }
        }

        cancel.setOnClickListener {
            var myintent = Intent(applicationContext, Dashboard::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }
    }
}