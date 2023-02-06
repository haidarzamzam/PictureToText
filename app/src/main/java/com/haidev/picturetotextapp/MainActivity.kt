package com.haidev.picturetotextapp

import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.haidev.picturetotextapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var scannedBitmap: Bitmap
    private var bitmapState: Bitmap? = null
    private lateinit var textResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        initUI()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    private fun initUI() {
        binding.tvResultCaptured.clearFocus()
        binding.btnTakePicture.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Choose action:")
            val pictureDialogItem = arrayOf(
                "Take from Gallery",
                "Take from Camera"
            )
            pictureDialog.setItems(pictureDialogItem) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            pictureDialog.show()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        launcherIntentCamera.launch(intent)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val bitmap = it.data?.extras?.get("data") as Bitmap
            scannedBitmap = bitmap
            bitmapState = bitmap
            Glide.with(this).load(bitmap).into(binding.ivCaptured)
        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (result.data?.data != null) {
                scannedBitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data?.data)
                bitmapState =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, result.data?.data)
            }
            Glide.with(this).load(result.data?.data).into(binding.ivCaptured)

            detectText(scannedBitmap)
        }
    }

    private fun detectText(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = imageFromBitmap(bitmap)

        recognizer.process(image)
            .addOnSuccessListener {
                binding.tvResultCaptured.text = it.text
                textResult = it.text
                Toast.makeText(this, "Success detect text", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Failed detect text", Toast.LENGTH_SHORT).show()
            }
    }

    private fun imageFromBitmap(bitmap: Bitmap): InputImage {
        return InputImage.fromBitmap(bitmap, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "No have permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            arrayOf(
                android.Manifest.permission.CAMERA
            )
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}