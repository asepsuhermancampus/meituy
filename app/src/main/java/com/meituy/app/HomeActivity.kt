package com.meituy.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnEditPhoto = findViewById<MaterialButton>(R.id.btnEditPhoto)
        val btnCamera = findViewById<MaterialButton>(R.id.btnCamera)

        btnEditPhoto.setOnClickListener {
            startActivity(Intent(this, PhotoEditorActivity::class.java))
        }

        btnCamera.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }
}
