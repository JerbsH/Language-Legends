package com.example.languagelegends.features

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.languagelegends.database.Converters

class ImagePickerActivity : ComponentActivity() {
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri
            Log.d("DBG", "Image picked from gallery")
            val returnIntent = Intent()
            uri?.let {
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                val converters = Converters()
                returnIntent.putExtra("image", converters.fromBitmap(bitmap))
            }
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            // Handle the returned Bitmap
            val returnIntent = Intent()
            val converters = Converters()
            returnIntent.putExtra("image", converters.fromBitmap(bitmap))
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.getStringExtra("requestType")) {
            "gallery" -> pickImage.launch("image/*")
            "camera" -> takePicture.launch(null)
        }
    }
}