package com.game.assemble

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameButton = findViewById<Button>(R.id.homePageGameButton)
        val leaderboardButton = findViewById<Button>(R.id.homePageLeaderboardButton)
        val profilePageButton = findViewById<Button>(R.id.homePageProfilePageButton)
        val encyclopediaButton = findViewById<Button>(R.id.homePageEncyclopediaButton)

        gameButton.setOnClickListener {
            val myIntent = Intent(
                this,
                GameActivity::class.java
            )
            startActivity(myIntent)
        }

        leaderboardButton.setOnClickListener {
            val myIntent = Intent(
                this,
                Leaderboard::class.java
            )
            startActivity(myIntent)
        }

        profilePageButton.setOnClickListener {
            val myIntent = Intent(
                this,
                ProfilePage::class.java
            )
            startActivity(myIntent)
        }
    }
}