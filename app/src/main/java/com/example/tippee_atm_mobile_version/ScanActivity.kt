package com.example.tippee_atm_mobile_version

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import org.opencv.core.Point
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream

class ScanActivity : AppCompatActivity() {

    private lateinit var resultText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        resultText = findViewById(R.id.textView)

        val byteArray = intent.getByteArrayExtra("pointsByteArray")
        val byteArrayInputStream = ByteArrayInputStream(byteArray)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        val serializablePoints = objectInputStream.readObject() as? List<SerializablePoint>
        val pointsList = serializablePoints?.map { it.toPoint() }

        resultText.text = pointsList.toString()

        val dbHelper = MinutiaeDatabaseHelper(this)
        val minutiaeDataList = dbHelper.getAllMinutiaeData()

        val minutiaeData = pointsList // obtain minutiae data from fingerprint scanner
        val bestMatch = minutiaeData?.let { dbHelper.compareFingerprints(it) }

        if (bestMatch != null && bestMatch.second > 0.67) {
            Toast.makeText(applicationContext, "Match found: ${bestMatch.first}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(applicationContext, "No match found", Toast.LENGTH_SHORT).show()
        }
    }
}
