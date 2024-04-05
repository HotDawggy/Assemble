package com.game.assemble

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.game.assemble.R

class ViewImageRunHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.run_history_display)

        val imageView = findViewById<ImageView>(R.id.runHistoryImage)
        val imageFileName = intent.getStringExtra("fileName")
        val resourceId = resources.getIdentifier(imageFileName, "drawable", packageName)
        imageView.setImageResource(resourceId)
    }
}