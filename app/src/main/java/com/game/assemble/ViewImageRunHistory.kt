package com.game.assemble

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity


class ViewImageRunHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.run_history_display)

        // set image path
        val imageView = findViewById<ImageView>(R.id.runHistoryImage)
        val imageFileName = intent.getStringExtra("fileName")
        val resourceId = resources.getIdentifier(imageFileName, "drawable", packageName)
        imageView.setImageResource(resourceId)

        // setup back button
        val buttonView = findViewById<Button>(R.id.runHistoryBackButton)
        buttonView.setOnClickListener {
            val myIntent = Intent(
                this,
                ProfilePage::class.java
            )
            startActivity(myIntent)
        }

        val leftButton = findViewById<Button>(R.id.leftButton)
        val rightButton = findViewById<Button>(R.id.rightButton)
        // set arrow path
        if (imageFileName == "examplehistory") {
            // next image exists
            leftButton.setBackgroundColor(Color.GRAY)
            rightButton.setOnClickListener {
                val myIntent = Intent(
                    this,
                    ViewImageRunHistory::class.java
                )
                myIntent.putExtra("fileName", "examplehistory2")
                startActivity(myIntent)
            }
        }
        else {
            // prev image exists
            rightButton.setBackgroundColor(Color.GRAY)
            leftButton.setOnClickListener {
                val myIntent = Intent(
                    this,
                    ViewImageRunHistory::class.java
                )
                myIntent.putExtra("fileName", "examplehistory")
                startActivity(myIntent)
            }
        }
    }
}