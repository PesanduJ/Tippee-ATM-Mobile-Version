package com.example.tippee_atm_mobile_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Deposit : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deposit)

        //Date & Time
        var time = findViewById<TextView>(R.id.time)
        var date = findViewById<TextView>(R.id.date)
        val dateTime = DateTime()
        time.text=dateTime.getTimeString()
        date.text= dateTime.getDateString()

        var amountToAdd = findViewById<EditText>(R.id.amount)
        var depositMoney = findViewById<Button>(R.id.depositMoney)
        var cancel = findViewById<Button>(R.id.cancel)

        val userAccountNumber = intent.getStringExtra("UserAccountNumber")

        depositMoney.setOnClickListener {
            val dbHelper = MinutiaeDatabaseHelper(applicationContext)
            val success = userAccountNumber?.let { dbHelper.addAmountByAccountNo(it.toInt(), amountToAdd.text.toString().toInt()) }
            if (success == true) {
                Toast.makeText(applicationContext, "Deposit successful!", Toast.LENGTH_SHORT).show()
                var myintent = Intent(applicationContext, Dashboard::class.java)
                myintent.putExtra("UserAccountNumber",userAccountNumber)
                startActivity(myintent)
            } else {
                Toast.makeText(applicationContext, "Unsuccessful, Please try again!", Toast.LENGTH_SHORT).show()
            }
        }

        cancel.setOnClickListener {
            var myintent = Intent(applicationContext, Dashboard::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }
    }
}