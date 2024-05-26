package com.dicoding.sub1.ui

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dicoding.sub1.dataStore
import com.dicoding.sub1.databinding.ActivityAddStoryBinding
import com.dicoding.sub1.preference.UserPreferences
import com.dicoding.sub1.viewmodel.AddStoryViewModel
import com.dicoding.sub1.viewmodel.LoginViewModel
import com.dicoding.sub1.viewmodel.ViewModelFactory
import com.google.android.gms.maps.model.LatLng
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var token: String

    private var getFile: File? = null
    private lateinit var fileFinal: File
    private var latlng: LatLng? = null
    private val addStoryViewModel: AddStoryViewModel by lazy {
        ViewModelProvider(this)[AddStoryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Post Story"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val preferences = UserPreferences.getInstance(dataStore)
        val userLoginViewModel =
            ViewModelProvider(this, ViewModelFactory(preferences))[LoginViewModel::class.java]

        userLoginViewModel.getToken().observe(this) {
            token = it
        }

        addStoryViewModel.message.observe(this) {
            showToast(it)
        }

        setUpListener()
    }

    private fun setUpListener() {
        binding.btnUpload.setOnClickListener {
            uploadStory()
        }
        binding.btnCamera.setOnClickListener {
            startTakePhoto()
        }
        binding.btnGallery.setOnClickListener {
            startGallery()
        }
        binding.btnSelectLocation.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.let { data ->
                    val lat = data.getDoubleExtra("selected_lat", 0.0)
                    val lng = data.getDoubleExtra("selected_lng", 0.0)
                    latlng = LatLng(lat, lng)
                    Log.d("AddStoryActivity", "Location received: $latlng")
                }
            }
        }

    private var anyPhoto = false
    private lateinit var currentPhotoPath: String
    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            getFile = myFile
            val result = BitmapFactory.decodeFile(myFile.path)
            anyPhoto = true
            binding.ivStory.setImageBitmap(result)
            binding.tvDes.requestFocus()
        }
    }

    private val timeStamp: String = SimpleDateFormat(
        FILENAME_FORMAT,
        Locale.US
    ).format(System.currentTimeMillis())

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)
        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@AddStoryActivity,
                "com.dicoding.sub1.FileProvider",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream: OutputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddStoryActivity)
            getFile = myFile
            binding.ivStory.setImageURI(selectedImg)
            binding.tvDes.requestFocus()
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun showToast(msg: String) {
        Toast.makeText(
            this@AddStoryActivity,
            StringBuilder("Message : ").append(msg),
            Toast.LENGTH_SHORT
        ).show()

        if (msg == "Story created successfully") {
            val intent = Intent(this@AddStoryActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        finish()
        return super.onSupportNavigateUp()
    }

    private fun uploadStory() {
        val des = binding.tvDes.text.toString().trim()
        if (getFile == null || des.isEmpty()) {
            showToast(if (getFile == null) "Please upload a photo" else "Description cannot be empty")
            binding.tvDes.error = if (getFile != null) "Description cannot be empty" else null
            return
        }

        if (latlng == null) {
            showToast("Please select a location")
            return
        }

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val file = getFile as File

                var compressedFile: File? = null
                var compressedFileSize = file.length()

                // Compress the file until its size is less than or equal to 1MB
                while (compressedFileSize > 1 * 1024 * 1024) {
                    compressedFile = withContext(Dispatchers.Default) {
                        Compressor.compress(applicationContext, file)
                    }
                    compressedFileSize = compressedFile.length()
                }

                fileFinal = compressedFile ?: file
            }

            // use the upload file to upload to server
            val requestImageFile =
                fileFinal.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                fileFinal.name,
                requestImageFile
            )

            val desPart = des.toRequestBody("text/plain".toMediaType())

            addStoryViewModel.upload(imageMultipart, desPart, latlng?.latitude, latlng?.longitude, token)
        }
    }

    companion object {
        const val FILENAME_FORMAT = "MMddyyyy"
    }
}
