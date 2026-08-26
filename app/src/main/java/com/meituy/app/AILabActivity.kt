package com.meituy.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class AILabActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_lab)

        val cardAIEnhance = findViewById<CardView>(R.id.cardAIEnhance)
        val cardAIPortrait = findViewById<CardView>(R.id.cardAIPortrait)
        val cardStyleStudio = findViewById<CardView>(R.id.cardStyleStudio)
        val cardAIRelight = findViewById<CardView>(R.id.cardAIRelight)

        // ponytail: AI Portrait & AI Relight butuh ML kit/face detection — toast dulu, tambah saat fitur di pesan.
        cardAIEnhance.setOnClickListener {
            startActivity(Intent(this, PhotoEditorActivity::class.java))
        }

        cardAIPortrait.setOnClickListener {
            Toast.makeText(this, R.string.ai_portrait, Toast.LENGTH_SHORT).show()
        }

        cardStyleStudio.setOnClickListener {
            startActivity(Intent(this, PhotoEditorActivity::class.java))
        }

        cardAIRelight.setOnClickListener {
            Toast.makeText(this, R.string.ai_relight, Toast.LENGTH_SHORT).show()
        }
    }
}
