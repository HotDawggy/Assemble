package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        // from https://stackoverflow.com/questions/44148852/how-to-add-a-button-dynamically-in-android-using-kotlin
        val runHistoryLayout = findViewById<LinearLayout>(R.id.profilePageRunHistoryLayout)
        val btnRunHistory = Button(this)
        btnRunHistory.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        btnRunHistory.text = "Run 1 - Apr 05"
        btnRunHistory.setOnClickListener {
            // from https://stackoverflow.com/questions/2405120/how-to-start-an-intent-by-passing-some-parameters-to-it
            val myIntent = Intent(
                this,
                ViewImageRunHistory::class.java
            )
            myIntent.putExtra("fileName", "examplehistory")
            startActivity(myIntent)
        }
        runHistoryLayout.addView(btnRunHistory)
    }
}