package com.example.animaldetector

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.animaldetector.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech
    private var selectedBitmap: Bitmap? = null

    private val CAMERA_PERMISSION_CODE = 100

    // YOLOv8 input size
    private val INPUT_SIZE = 640

    private var tflite: Interpreter? = null

    private val labels = mutableListOf<String>()

    // Gallery picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    // Camera capture
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { handleCapturedImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        setupUI()

        loadModel()

        loadLabels()
    }

    private fun setupUI() {

        binding.btnCircle.setOnClickListener { view ->
            showImageSourceMenu(view)
        }

        binding.btnPredict.setOnClickListener {

            if (selectedBitmap != null) {
                predictAnimal()
            } else {
                Toast.makeText(
                    this,
                    "প্ৰথমে এটা ছবি দিয়ক",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showImageSourceMenu(view: View) {

        val popup = PopupMenu(this, view)

        popup.menuInflater.inflate(
            R.menu.image_source_menu,
            popup.menu
        )

        popup.setOnMenuItemClickListener { item ->

            when (item.itemId) {

                R.id.menu_camera -> {

                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }

                    true
                }

                R.id.menu_gallery -> {

                    openGallery()

                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun checkCameraPermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == CAMERA_PERMISSION_CODE) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                openCamera()

            } else {

                Toast.makeText(
                    this,
                    "কেমেরা অনুমতি প্রয়োজন",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openCamera() {

        takePictureLauncher.launch(null)
    }

    private fun openGallery() {

        pickImageLauncher.launch("image/*")
    }

    private fun handleSelectedImage(imageUri: Uri) {

        try {

            val inputStream =
                contentResolver.openInputStream(imageUri)

            selectedBitmap =
                BitmapFactory.decodeStream(inputStream)

            inputStream?.close()

            binding.ivImagePreview.setImageBitmap(selectedBitmap)

            binding.ivImagePreview.visibility = View.VISIBLE

            binding.tvImagePlaceholder.visibility = View.GONE

            binding.tvResult.text = ""

        } catch (e: Exception) {

            Log.e("MainActivity", "Error loading image", e)

            Toast.makeText(
                this,
                "ছবি লোড করতে ব্যর্থ",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleCapturedImage(bitmap: Bitmap) {

        selectedBitmap = bitmap

        binding.ivImagePreview.setImageBitmap(bitmap)

        binding.ivImagePreview.visibility = View.VISIBLE

        binding.tvImagePlaceholder.visibility = View.GONE

        binding.tvResult.text = ""
    }

    private fun predictAnimal() {

        binding.tvDetecting.visibility = View.VISIBLE

        binding.btnPredict.isEnabled = false

        binding.tvResult.text = ""

        binding.tvDetecting.postDelayed({

            binding.tvDetecting.visibility = View.GONE

            binding.btnPredict.isEnabled = true

            selectedBitmap?.let {

                val result = runModelPrediction(it)

                binding.tvResult.text = result

                if (
                    result != "কোনো জীৱ-জন্তু ধৰা পৰা নাই" &&
                    result != "ত্রুটি"
                ) {

                    if (result.contains(" আৰু ")) {

                        speakOut("মই দেখিছোঁ $result")

                    } else {

                        speakOut("এইটো হয় $result")
                    }
                }
            }

        }, 2000)
    }

    private fun loadModel() {

        try {

            val fileDescriptor =
                assets.openFd("best_float32.tflite")

            val inputStream =
                FileInputStream(fileDescriptor.fileDescriptor)

            val fileChannel = inputStream.channel

            val modelBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )

            val options = Interpreter.Options()

            options.setNumThreads(4)

            tflite = Interpreter(modelBuffer, options)

            Log.d("YOLO", "Model loaded")

        } catch (e: Exception) {

            Log.e("YOLO", "Model load error", e)
        }
    }

    private fun loadLabels() {

        try {

            assets.open("labels.txt")
                .bufferedReader()
                .useLines { lines ->

                    lines.forEach {

                        labels.add(it.trim())
                    }
                }

        } catch (e: Exception) {

            Log.e("YOLO", "Label load error", e)
        }
    }

// CORRECTED VERSION FOR YOUR MODEL
// Output shape: (1, 15, 8400) where 15 = 11 classes + 4 bbox coords

    private fun runModelPrediction(bitmap: Bitmap): String {

        return try {

            val scaledBitmap =
                Bitmap.createScaledBitmap(
                    bitmap,
                    INPUT_SIZE,
                    INPUT_SIZE,
                    true
                )

            val byteBuffer =
                convertBitmapToByteBuffer(scaledBitmap)

            // YOUR MODEL OUTPUT: (1, 15, 8400)
            // NOT (1, 84, 8400) like COCO!
            // 15 = 11 classes + 4 bbox (x, y, w, h)
            val output = Array(1) { Array(15) { FloatArray(8400) } }

            tflite?.run(byteBuffer, output)

            Log.d("YOLO", "✅ Model output shape: [1][15][8400]")

            val detectedAnimals = mutableListOf<String>()
            val confidenceThreshold = 0.4f

            // Loop through all 8400 predictions
            for (i in 0 until 8400) {

                // Indices 0-3 are bbox coordinates (x, y, w, h)
                // Indices 4-14 are class confidence scores (11 classes)

                var maxScore = 0f
                var classId = -1

                // Check all 11 classes (indices 4 to 14)
                for (c in 4 until 15) {

                    val score = output[0][c][i]

                    if (score > maxScore) {
                        maxScore = score
                        classId = c - 4  // class 0-10
                    }
                }

                // If confidence is high enough and valid class
                if (maxScore > confidenceThreshold && classId >= 0 && classId < labels.size) {

                    val label = labels[classId]

                    // Avoid duplicates
                    if (!detectedAnimals.contains(label)) {
                        detectedAnimals.add(label)
                        Log.d("YOLO", "Detected: $label (confidence: $maxScore)")
                    }
                }
            }

            // Return results based on number of detections
            val result = when {

                detectedAnimals.isEmpty() ->
                    "কোনো প্রাণী সনাক্ত হয়নি"

                detectedAnimals.size == 1 ->
                    detectedAnimals[0]

                detectedAnimals.size == 2 ->
                    "${detectedAnimals[0]} আৰু ${detectedAnimals[1]}"

                else ->
                    detectedAnimals.joinToString(", ")
            }

            Log.d("YOLO", "Final Result: $result")
            result

        } catch (e: Exception) {

            Log.e("YOLO", "Prediction error: ${e.message}", e)
            e.printStackTrace()

            "ত্রুটি"
        }
    }

    private fun convertBitmapToByteBuffer(
        bitmap: Bitmap
    ): ByteBuffer {

        val byteBuffer =
            ByteBuffer.allocateDirect(
                1 * INPUT_SIZE * INPUT_SIZE * 3 * 4
            )

        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues =
            IntArray(INPUT_SIZE * INPUT_SIZE)

        bitmap.getPixels(
            intValues,
            0,
            INPUT_SIZE,
            0,
            0,
            INPUT_SIZE,
            INPUT_SIZE
        )

        var pixel = 0

        for (i in 0 until INPUT_SIZE) {

            for (j in 0 until INPUT_SIZE) {

                val value = intValues[pixel++]

                byteBuffer.putFloat(
                    ((value shr 16) and 0xFF) / 255f
                )

                byteBuffer.putFloat(
                    ((value shr 8) and 0xFF) / 255f
                )

                byteBuffer.putFloat(
                    (value and 0xFF) / 255f
                )
            }
        }

        return byteBuffer
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.language = Locale.US
        }
    }

    private fun speakOut(text: String) {

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            ""
        )
    }

    override fun onDestroy() {

        super.onDestroy()

        tflite?.close()

        tts.shutdown()
    }
}