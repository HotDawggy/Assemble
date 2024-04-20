package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.internal.userAgent

class ProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_page)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        // init username
        val usernameEditText: EditText = findViewById(R.id.profilePageUsername)
        val savedUsername = sharedPrefs.getString("username", "")
        if (savedUsername == "") {
            usernameEditText.setHint("Enter Username :3")
        }
        else {
            usernameEditText.setText(savedUsername)
        }

        // init high score
        val highScoreTextView: TextView = findViewById(R.id.profilePageHighScore)
        highScoreTextView.text = "High Score: " + sharedPrefs.getString("highScore", "0")

        // init games played
        val gamesPlayedTextView: TextView = findViewById(R.id.profilePageGamesPlayedCount)
        gamesPlayedTextView.text = "Total Games Played: " + sharedPrefs.getInt("gamesPlayed", 0).toString()

        // init fav asm instruction
        val famAsmTextView: TextView = findViewById(R.id.profilePageFavAsm)
        famAsmTextView.text = "You used ${sharedPrefs.getString("favAsm", "XOR")} ${sharedPrefs.getInt("favAsmCount", 0)} times!"

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