package com.example.tippee_atm_mobile_version

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
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
    }
}