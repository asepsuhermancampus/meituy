package com.meituy.app

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSaveImage: Button
    private lateinit var progressBar: ProgressBar

    private var currentBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null
    private var currentFilter: FilterType = FilterType.ORIGINAL
    private lateinit var filterAdapter: FilterAdapter

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            selectImageFromGallery()
        } else {
            Toast.makeText(this, "Storage permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        filterRecyclerView = findViewById(R.id.filterRecyclerView)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSaveImage = findViewById(R.id.btnSaveImage)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = android.view.View.GONE
    }

    private fun setupRecyclerView() {
        val filters = FilterAdapter.createDefaultList()
        filterAdapter = FilterAdapter(filters) { selectedFilter ->
            applyFilter(selectedFilter)
        }
        
        filterRecyclerView.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.HORIZONTAL,
            false
        )
        filterRecyclerView.adapter = filterAdapter
    }

    private fun setupClickListeners() {
        btnSelectImage.setOnClickListener {
            checkStoragePermission()
        }

        btnSaveImage.setOnClickListener {
            if (currentBitmap != null) {
                saveImage()
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkStoragePermission() {
        val readPermission = checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission = checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (readPermission != PackageManager.PERMISSION_GRANTED || 
            writePermission != PackageManager.PERMISSION_GRANTED) {
            requestStoragePermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        } else {
            selectImageFromGallery()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                progressBar.visibility = android.view.View.VISIBLE
                GlobalScope.launch(Dispatchers.Default) {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                    
                    originalBitmap?.recycle()
                    currentBitmap?.recycle()
                    
                    originalBitmap = bitmap
                    currentBitmap = bitmap.copy(bitmap.config, true)
                    
                    runOnUiThread {
                        imageView.setImageBitmap(currentBitmap)
                        filterAdapter.setSelectedFilter(FilterType.ORIGINAL)
                        currentFilter = FilterType.ORIGINAL
                        progressBar.visibility = android.view.View.GONE
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = android.view.View.GONE
                }
            }
        }
    }

    private fun selectImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun applyFilter(filterType: FilterType) {
        currentFilter = filterType
        originalBitmap?.let { original ->
            progressBar.visibility = android.view.View.VISIBLE
            btnSelectImage.isEnabled = false
            btnSaveImage.isEnabled = false
            
            GlobalScope.launch(Dispatchers.Default) {
                val workBitmap = original.copy(original.config, true)
                val filtered = FilterEngine.applyFilter(workBitmap, filterType)
                
                currentBitmap?.recycle()
                currentBitmap = filtered
                
                runOnUiThread {
                    imageView.setImageBitmap(currentBitmap)
                    progressBar.visibility = android.view.View.GONE
                    btnSelectImage.isEnabled = true
                    btnSaveImage.isEnabled = true
                }
            }
        }
    }

    private fun saveImage() {
        if (currentBitmap == null) return

        progressBar.visibility = android.view.View.VISIBLE
        btnSaveImage.isEnabled = false

        GlobalScope.launch(Dispatchers.Default) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "Meituy_${timestamp}_${currentFilter.displayName}"
                
                val savedImageUri = MediaStore.Images.Media.insertImage(
                    contentResolver,
                    currentBitmap,
                    fileName,
                    "Photo edited with Meituy - Filter: ${currentFilter.displayName}"
                )

                runOnUiThread {
                    if (savedImageUri != null) {
                        Toast.makeText(this@MainActivity, "Image saved successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to save image", Toast.LENGTH_SHORT).show()
                    }
                    progressBar.visibility = android.view.View.GONE
                    btnSaveImage.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = android.view.View.GONE
                    btnSaveImage.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        originalBitmap?.recycle()
    }
}