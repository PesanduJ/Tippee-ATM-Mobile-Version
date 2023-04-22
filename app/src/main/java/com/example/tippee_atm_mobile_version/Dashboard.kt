package com.example.tippee_atm_mobile_version

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class Dashboard : AppCompatActivity() {

    private lateinit var welcome: TextView
    private lateinit var lastWithdrawalAmount : Button
    private lateinit var transfer : Button
    private lateinit var deposit : Button
    private lateinit var balance : Button
    private lateinit var withdraw : Button
    private lateinit var exit : Button
    private lateinit var tipppeeATM: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        welcome = findViewById(R.id.welcome)
        lastWithdrawalAmount = findViewById(R.id.lastWithdrawalAmount)
        transfer = findViewById(R.id.transfer)
        deposit = findViewById(R.id.deposit)
        balance = findViewById(R.id.balance)
        withdraw = findViewById(R.id.withdraw)
        exit = findViewById(R.id.exit)
        tipppeeATM = findViewById(R.id.tipppeeATM)

        //Date & Time
        var time = findViewById<TextView>(R.id.time)
        var date = findViewById<TextView>(R.id.date)
        val dateTime = DateTime()
        time.text=dateTime.getTimeString()
        date.text= dateTime.getDateString()


        val userAccountNumber = intent.getStringExtra("UserAccountNumber")

        val dbHelper = MinutiaeDatabaseHelper(applicationContext)

        val userData = userAccountNumber?.let { dbHelper.getUserDataByAccountNo(it.toInt()) }


        userData?.let {
            val name = it.first
            val amount = it.second.first
            val lastTransaction = it.second.second

            // Do something with the extracted values
            welcome.text="Welcome, Mr. ${name}"
            lastWithdrawalAmount.text="${lastTransaction} $"
        }

        transfer.setOnClickListener {
            var myintent = Intent(applicationContext, Transfer::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }

        deposit.setOnClickListener {
            var myintent = Intent(applicationContext, Deposit::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }

        balance.setOnClickListener {
            var myintent = Intent(applicationContext, Balance::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }

        withdraw.setOnClickListener {
            var myintent = Intent(applicationContext, Withdraw::class.java)
            myintent.putExtra("UserAccountNumber",userAccountNumber)
            startActivity(myintent)
        }

        exit.setOnClickListener {
            finish()
        }

        tipppeeATM.setOnClickListener {
            var myintent = Intent(applicationContext, MainActivity::class.java)
            startActivity(myintent)
        }


    }
}