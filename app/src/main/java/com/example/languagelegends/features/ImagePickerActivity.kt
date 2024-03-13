package com.example.languagelegends.features

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.languagelegends.database.AppDatabase
import com.example.languagelegends.database.Converters
import com.example.languagelegends.database.DatabaseProvider
import com.example.languagelegends.database.UserProfileDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * This class represents an activity for picking an image from the gallery or taking a photo.
 * It uses the AndroidX Activity Result API to handle the result of the image picking or photo taking process.
 * It also uses the Room database to check if the username exists in the database.
 */
class ImagePickerActivity : ComponentActivity() {

    private val appDatabase: AppDatabase by lazy {
        DatabaseProvider.getDatabase(applicationContext)
    }

    private val userProfileDao: UserProfileDao by lazy {
        appDatabase.userProfileDao()
    }

    // Register an activity result contract for picking an image from the gallery
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                Log.d("DBG", "Image picked from gallery")
                val returnIntent = Intent()
                uri.let {
                    @Suppress("DEPRECATION")
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    val converters = Converters()
                    returnIntent.putExtra("image", converters.fromBitmap(bitmap))
                }
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            } else {
                Log.d("DBG", "No image selected from gallery")
                finish()
            }
        }

    // Register an activity result contract for taking a photo
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                val returnIntent = Intent()
                val converters = Converters()
                returnIntent.putExtra("image", converters.fromBitmap(bitmap))
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            } else {
                Log.d("DBG", "No photo taken")
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username")

        lifecycleScope.launch {
            val userProfile = withContext(Dispatchers.IO) {
                userProfileDao.getAllUserProfiles().firstOrNull { it.username == username }
            }

            if (userProfile != null) {
                when (intent.getStringExtra("requestType")) {
                    "gallery" -> pickImage.launch("image/*")
                    "camera" -> takePicture.launch(null)
                }
            } else {
                withContext(Dispatchers.Main) {
                    val toast = Toast.makeText(
                        this@ImagePickerActivity,
                        "Please add username first.",
                        Toast.LENGTH_SHORT,
                    )
                    toast.show()
                }
                finish()
            }
        }
    }
}