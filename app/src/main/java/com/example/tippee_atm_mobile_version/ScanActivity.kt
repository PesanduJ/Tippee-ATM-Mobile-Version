package com.example.tippee_atm_mobile_version

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class ScanActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var fingerprintButton: Button
    private lateinit var fingerprintImageView: ImageView

    var scannedMinutiaeValue = mutableListOf<Point>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        fingerprintButton = findViewById(R.id.fingerprint_button)

        fingerprintButton.setOnClickListener {
            val biometricManager = BiometricManager.from(this)
            when (biometricManager.canAuthenticate()) {
                BiometricManager.BIOMETRIC_SUCCESS -> {
                    // Biometric authentication can be performed
                    startBiometricAuthentication()
                }
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                    // No biometric features available on this device
                    resultText.text=""
                    resultText.text="No biometric features available on this device"

                }
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                    // Biometric features are currently unavailable
                    resultText.text=""
                    resultText.text="Biometric features are currently unavailable"
                }
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                    // The user hasn't associated any biometric credentials with their account
                    resultText.text=""
                    resultText.text="The user hasn't associated any biometric credentials with their account"
                }
            }
        }
    }

    private fun startBiometricAuthentication() {
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            "MyKeyAlias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .build()

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
        )
        keyGenerator.init(keyGenParameterSpec)
        val secretKey = keyGenerator.generateKey()

        val cipher = Cipher.getInstance(
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
        )

        val biometricPrompt = BiometricPrompt(
            this,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // Handle successful authentication here
                    val scannedFingerprint = result.cryptoObject?.cipher?.doFinal()
                    val binaryFingerprint = scannedFingerprint?.joinToString("") {
                        "%8s".format(it.toUInt().toString(2)).replace(' ', '0')
                    }
                    // Set the binary fingerprint in the text field if it's not null
                    if (binaryFingerprint != null) {
                        resultText.text = binaryFingerprint

                        val fingerprintBitmap = generateFingerprintBitmap(binaryFingerprint)

                        val enhancedBitmap = enhanceFingerprintImage(fingerprintBitmap)

                        val mat = Mat()
                        Utils.bitmapToMat(enhancedBitmap, mat)
                        val segmentedMat = segmentFingerprintImage(mat)

                        val segmentedBitmap = Bitmap.createBitmap(segmentedMat.cols(), segmentedMat.rows(), Bitmap.Config.ARGB_8888)
                        Utils.matToBitmap(segmentedMat, segmentedBitmap)

                        val extractedMinutiae = extractMinutiae(segmentedMat)
                        scannedMinutiaeValue.addAll(extractedMinutiae)
                        resultText.text = extractedMinutiae.toString()

                        fingerprintImageView.setImageBitmap(segmentedBitmap)

                        val dbHelper = MinutiaeDatabaseHelper(applicationContext)
                        val minutiaeDataList = dbHelper.getAllMinutiaeData()

                        val minutiaeData =  extractedMinutiae// obtain minutiae data from fingerprint scanner
                        val bestMatch = minutiaeData?.let { dbHelper.compareFingerprints(it) }

                        if (bestMatch != null && bestMatch.second > 0.67) {
                            //Toast.makeText(applicationContext, "Match found: ${bestMatch.first}", Toast.LENGTH_SHORT).show()

                            var myintent = Intent(applicationContext, Dashboard::class.java)
                            myintent.putExtra("AccountNo",bestMatch.first)
                            startActivity(myintent)

                        } else {
                            Toast.makeText(applicationContext, "Please place your finger again!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Handle authentication error here
                }

                override fun onAuthenticationFailed() {
                    // Handle authentication failure here
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Scan your fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cryptoObject = BiometricPrompt.CryptoObject(cipher)

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun generateFingerprintBitmap(binaryFingerprint: String): Bitmap {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        for (i in 0 until binaryFingerprint.length step 8) {
            val byteString = binaryFingerprint.substring(i, i + 8)
            val pixelValue = Integer.parseInt(byteString, 2)
            val x = (i / 8) % 256
            val y = (i / 8) / 256
            bitmap.setPixel(x, y, Color.rgb(pixelValue, pixelValue, pixelValue))
        }
        return bitmap
    }

    private fun enhanceFingerprintImage(bitmap: Bitmap): Bitmap {
        val inputMat = Mat(bitmap.height, bitmap.width, CvType.CV_8UC1)
        Utils.bitmapToMat(bitmap, inputMat)

        // Convert image to grayscale
        val grayMat = Mat()
        Imgproc.cvtColor(inputMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // Apply histogram equalization to enhance contrast
        Imgproc.equalizeHist(grayMat, grayMat)

        // Apply Gaussian blur to remove noise
        val filteredMat = Mat()
        Imgproc.GaussianBlur(grayMat, filteredMat, Size(5.0, 5.0), 0.0)

        // Apply adaptive thresholding to separate ridges and valleys
        val thresholdedMat = Mat()
        Imgproc.adaptiveThreshold(filteredMat, thresholdedMat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)

        // Invert the thresholded image to make the ridges white and valleys black
        Core.bitwise_not(thresholdedMat, thresholdedMat)

        // Convert the processed Mat back to a Bitmap and return it
        val outputBitmap = Bitmap.createBitmap(thresholdedMat.cols(), thresholdedMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(thresholdedMat, outputBitmap)
        return outputBitmap
    }

    private fun segmentFingerprintImage(inputImage: Mat): Mat {
        // Convert to grayscale
        val grayImage = Mat()
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY)

        // Apply Gaussian blur
        val blurredImage = Mat()
        Imgproc.GaussianBlur(grayImage, blurredImage, Size(5.0, 5.0), 0.0)

        // Apply adaptive thresholding
        val thresholdedImage = Mat()
        Imgproc.adaptiveThreshold(blurredImage, thresholdedImage, 255.0, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)

        // Apply morphological operations
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, Size(3.0, 3.0))
        val openedImage = Mat()
        Imgproc.morphologyEx(thresholdedImage, openedImage, Imgproc.MORPH_OPEN, kernel)

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(openedImage, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        // Filter contours
        val filteredContours = mutableListOf<MatOfPoint>()
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > 100) { // Filter out small contours
                filteredContours.add(contour)
            }
        }

        // Draw contours on black image
        val segmentedFingerprint = Mat.zeros(inputImage.size(), CvType.CV_8UC1)
        val contourIndex = -1 // Draw all contours
        val color = Scalar(255.0, 255.0, 255.0) // White color
        Imgproc.drawContours(segmentedFingerprint, filteredContours, contourIndex, color, -1)

        return segmentedFingerprint
    }

    fun extractMinutiae(segmentedImage: Mat): List<Point> {
        val minutiae = mutableListOf<Point>()
        val cnImage = Mat(segmentedImage.rows(), segmentedImage.cols(), CvType.CV_8UC1, Scalar(0.0))

        // Apply the CN algorithm to the segmented fingerprint image
        for (i in 1 until segmentedImage.rows() - 1) {
            for (j in 1 until segmentedImage.cols() - 1) {
                if (segmentedImage.get(i, j)[0] > 0) {
                    val cn = getCrossingNumber(segmentedImage, i, j)
                    if (cn == 1 || cn == 3) {
                        minutiae.add(Point(j.toDouble(), i.toDouble()))
                        cnImage.put(i, j, 255.0)
                    } else if (cn == 2 || cn == 4) {
                        cnImage.put(i, j, 127.0)
                    }
                }
            }
        }

        // Display the CN image for debugging purposes
        Imgproc.cvtColor(cnImage, cnImage, Imgproc.COLOR_GRAY2RGBA)
        Imgproc.putText(cnImage, "CN Image", Point(10.0, 30.0), Imgproc.FONT_HERSHEY_SIMPLEX, 1.0, Scalar(255.0, 0.0, 0.0), 2)
        Imgcodecs.imwrite("CN_Image.jpg", cnImage)

        return minutiae
    }

    fun getCrossingNumber(img: Mat, i: Int, j: Int): Int {
        var cn = 0
        val p1 = img.get(i - 1, j - 1)[0]
        val p2 = img.get(i - 1, j)[0]
        val p3 = img.get(i - 1, j + 1)[0]
        val p4 = img.get(i, j + 1)[0]
        val p5 = img.get(i + 1, j + 1)[0]
        val p6 = img.get(i + 1, j)[0]
        val p7 = img.get(i + 1, j - 1)[0]
        val p8 = img.get(i, j - 1)[0]

        if (p1 == 0.0 && p2 > 0.0) cn++
        if (p2 == 0.0 && p3 > 0.0) cn++
        if (p3 == 0.0 && p4 > 0.0) cn++
        if (p4 == 0.0 && p5 > 0.0) cn++
        if (p5 == 0.0 && p6 > 0.0) cn++
        if (p6 == 0.0 && p7 > 0.0) cn++
        if (p7 == 0.0 && p8 > 0.0) cn++
        if (p8 == 0.0 && p1 > 0.0) cn++

        return cn
    }
}




















/*
*resultText = findViewById(R.id.textView)

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
* */