package com.meituy.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import java.io.File

class CameraActivity : AppCompatActivity() {

    private var pendingUri: Uri? = null

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        if (success && uri != null) {
            startActivity(Intent(this, PhotoEditorActivity::class.java).setData(uri))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // ponytail: tab Photo/Video/Portrait masih placeholder; video butuh CameraX, tambah kalau fitur video dipesan.
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText(R.string.photo_mode))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.video_mode))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.portrait_mode))
        tabLayout.selectedTabPosition = 0

        val btnCapture = findViewById<MaterialButton>(R.id.btnCapture)

        btnCapture.setOnClickListener {
            val uri = createCaptureUri()
            pendingUri = uri
            takePicture.launch(uri)
        }
    }

    private fun createCaptureUri(): Uri {
        val dir = File(cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    }
}
