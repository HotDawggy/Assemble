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
    }
}