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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    private var currentBitmap: Bitmap? = null
    private var filteredBitmap: Bitmap? = null
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
                currentBitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                currentBitmap?.let { bitmap ->
                    filteredBitmap = bitmap.copy(bitmap.config, true)
                    imageView.setImageBitmap(bitmap)
                    filterAdapter.setSelectedFilter(FilterType.ORIGINAL)
                    currentFilter = FilterType.ORIGINAL
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun applyFilter(filterType: FilterType) {
        currentFilter = filterType
        filteredBitmap?.let { bitmap ->
            val filtered = FilterEngine.applyFilter(bitmap, filterType)
            imageView.setImageBitmap(filtered)
            currentBitmap = filtered.copy(filtered.config, true)
        }
    }

    private fun saveImage() {
        if (filteredBitmap == null) return

        val savedImageUri = MediaStore.Images.Media.insertImage(
            contentResolver,
            filteredBitmap,
            "Meituy_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}",
            "Photo edited with Meituy"
        )

        if (savedImageUri != null) {
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
}