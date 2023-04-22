package com.example.tippee_atm_mobile_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Transfer : AppCompatActivity() {

    private lateinit var accountNumber:EditText
    private lateinit var amount:EditText
    private lateinit var transferMoney:Button
    private lateinit var cancel:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transfer)

        //Date & Time
        var time = findViewById<TextView>(R.id.time)
        var date = findViewById<TextView>(R.id.date)
        val dateTime = DateTime()
        time.text=dateTime.getTimeString()
        date.text= dateTime.getDateString()

        accountNumber = findViewById(R.id.accountNumber)
        amount = findViewById(R.id.amount)
        transferMoney = findViewById(R.id.transferMoney)
        cancel = findViewById(R.id.cancel)


        val userAccountNumber = intent.getStringExtra("UserAccountNumber")

        transferMoney.setOnClickListener {
            val dbHelper = MinutiaeDatabaseHelper(applicationContext)
            if (userAccountNumber != null) {
                val transferSuccessful = dbHelper.transferAmount(userAccountNumber.toInt(), accountNumber.text.toString().toInt(), amount.text.toString().toInt())
                if (transferSuccessful) {
                    Toast.makeText(applicationContext, "Transfer successful!", Toast.LENGTH_SHORT).show()
                    var myintent = Intent(applicationContext, Dashboard::class.java)
                    myintent.putExtra("UserAccountNumber",userAccountNumber)
                    startActivity(myintent)
                } else {
                    Toast.makeText(applicationContext, "Transfer failed. Insufficient balance or invalid account number!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cancel.setOnClickListener {
            var myintent = Intent(applicationContext, Dashboard::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }

    }
}