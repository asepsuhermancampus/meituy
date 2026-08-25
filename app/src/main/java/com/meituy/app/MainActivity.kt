package com.meituy.app

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var filterRecyclerView: RecyclerView
    private lateinit var btnSelectImage: MaterialButton
    private lateinit var btnSaveImage: MaterialButton
    private lateinit var btnReset: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var placeholderText: TextView
    private lateinit var sliderContainer: View
    private lateinit var intensitySlider: Slider
    private lateinit var intensityLabel: TextView

    private var currentBitmap: Bitmap? = null
    private var originalBitmap: Bitmap? = null
    private var currentFilter: FilterType = FilterType.ORIGINAL
    private var currentIntensity: Float = 1.0f
    private lateinit var filterAdapter: FilterAdapter
    private var isProcessing: Boolean = false
    private var lastSavedUri: Uri? = null

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            selectImageFromGallery()
        } else {
            Toast.makeText(this, R.string.permission_required, Toast.LENGTH_SHORT).show()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                showLoading(true)
                lifecycleScope.launch(Dispatchers.Default) {
                    val bitmap = try {
                        val options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                            BitmapFactory.decodeStream(contentResolver.openInputStream(it), null, options)
                        }
                        
                        val newOptions = BitmapFactory.Options().apply {
                            inSampleSize = calculateInSampleSize(options, 2048, 2048)
                        }
                        BitmapFactory.decodeStream(contentResolver.openInputStream(it), null, newOptions)
                    } catch (e: Exception) {
                        null
                    }
                    
                    runOnUiThread {
                        if (bitmap != null) {
                            originalBitmap?.recycle()
                            currentBitmap?.recycle()
                            
                            originalBitmap = bitmap
                            currentBitmap = bitmap.copy(bitmap.config, true)
                            
                            imageView.setImageBitmap(currentBitmap)
                            filterAdapter.setSelectedFilter(FilterType.ORIGINAL)
                            currentFilter = FilterType.ORIGINAL
                            currentIntensity = 1.0f
                            intensitySlider.value = 100f
                            sliderContainer.visibility = View.GONE
                            showLoading(false)
                            updatePlaceholderVisibility()
                        } else {
                            Toast.makeText(this@MainActivity, R.string.error_loading_image, Toast.LENGTH_SHORT).show()
                            showLoading(false)
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, R.string.error_loading_image, Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupClickListeners()
        setupSlider()
        updatePlaceholderVisibility()
    }

    private fun initViews() {
        imageView = findViewById(R.id.imageView)
        filterRecyclerView = findViewById(R.id.filterRecyclerView)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSaveImage = findViewById(R.id.btnSaveImage)
        btnReset = findViewById(R.id.btnReset)
        progressBar = findViewById(R.id.progressBar)
        placeholderText = findViewById(R.id.placeholderText)
        sliderContainer = findViewById(R.id.sliderContainer)
        intensitySlider = findViewById(R.id.intensitySlider)
        intensityLabel = findViewById(R.id.intensityLabel)
    }

    private fun setupRecyclerView() {
        val filters = FilterAdapter.createDefaultList()
        filterAdapter = FilterAdapter(filters) { selectedFilter ->
            if (!isProcessing) {
                applyFilter(selectedFilter, currentIntensity)
            }
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
            if (!isProcessing) {
                checkStoragePermission()
            }
        }

        btnReset.setOnClickListener {
            if (!isProcessing && originalBitmap != null) {
                resetToOriginal()
            }
        }

        btnSaveImage.setOnClickListener {
            if (!isProcessing && currentBitmap != null) {
                saveImage()
            }
        }
    }

    private fun setupSlider() {
        intensitySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser && currentFilter != FilterType.ORIGINAL && !isProcessing) {
                currentIntensity = value / 100f
                applyFilter(currentFilter, currentIntensity, fromSlider = true)
            }
        }
    }

    private fun checkStoragePermission() {
        val readPermission = checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val writePermission = checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readMediaPermission = checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        } else {
            if (readMediaPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestStoragePermission.launch(permissionsToRequest.toTypedArray())
        } else {
            selectImageFromGallery()
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (width: Int, height: Int) = options.outWidth to options.outHeight
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun selectImageFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun applyFilter(filterType: FilterType, intensity: Float = 1.0f, fromSlider: Boolean = false) {
        if (originalBitmap == null) return
        
        currentFilter = filterType
        currentIntensity = intensity
        
        if (filterType == FilterType.ORIGINAL) {
            originalBitmap?.let { original ->
                currentBitmap?.recycle()
                currentBitmap = original.copy(original.config, true)
                imageView.setImageBitmap(currentBitmap)
                sliderContainer.visibility = View.GONE
            }
            return
        }
        
        sliderContainer.visibility = View.VISIBLE
        
        if (!fromSlider) {
            intensitySlider.value = 100f
            currentIntensity = 1.0f
        }
        
        originalBitmap?.let { original ->
            showLoading(true)
            isProcessing = true
            
            lifecycleScope.launch(Dispatchers.Default) {
                try {
                    val workBitmap = original.copy(original.config, true)
                    val filtered = FilterEngine.applyFilter(workBitmap, filterType, intensity)
                    
                    runOnUiThread {
                        currentBitmap?.recycle()
                        currentBitmap = filtered
                        imageView.setImageBitmap(currentBitmap)
                        showLoading(false)
                        isProcessing = false
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        showLoading(false)
                        isProcessing = false
                        Toast.makeText(this@MainActivity, "Error applying filter", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun resetToOriginal() {
        originalBitmap?.let { original ->
            currentBitmap?.recycle()
            currentBitmap = original.copy(original.config, true)
            imageView.setImageBitmap(currentBitmap)
            filterAdapter.setSelectedFilter(FilterType.ORIGINAL)
            currentFilter = FilterType.ORIGINAL
            currentIntensity = 1.0f
            intensitySlider.value = 100f
            sliderContainer.visibility = View.GONE
        }
    }

    private fun saveImage() {
        if (currentBitmap == null) return

        showLoading(true)
        btnSaveImage.isEnabled = false

        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val filterName = currentFilter.displayName.replace(" ", "_")
                val fileName = "Meituy_${filterName}_${timestamp}"
                
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Meituy")
                    }
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                
                runOnUiThread {
                    if (uri != null) {
                        lastSavedUri = uri
                        try {
                            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
                            outputStream?.use {
                                currentBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, it)
                            }
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, R.string.image_saved, Toast.LENGTH_SHORT).show()
                                showLoading(false)
                                btnSaveImage.isEnabled = true
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                                showLoading(false)
                                btnSaveImage.isEnabled = true
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, R.string.image_save_error, Toast.LENGTH_SHORT).show()
                            showLoading(false)
                            btnSaveImage.isEnabled = true
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                    btnSaveImage.isEnabled = true
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSelectImage.isEnabled = !show
        btnSaveImage.isEnabled = !show
        btnReset.isEnabled = !show && originalBitmap != null
    }

    private fun updatePlaceholderVisibility() {
        placeholderText.visibility = if (currentBitmap == null) View.VISIBLE else View.GONE
        btnReset.isEnabled = originalBitmap != null && !isProcessing
    }

    override fun onDestroy() {
        super.onDestroy()
        currentBitmap?.recycle()
        originalBitmap?.recycle()
    }
}