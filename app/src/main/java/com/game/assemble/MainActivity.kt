package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        findViewById<Typewriter>(R.id.homeTitleTypewriter).also { it.setDelay(50) ; it.animateText("ASSEMBLE!", true) }
        findViewById<Typewriter>(R.id.homeAuthorTypewriter).also { it.setDelay(10) ; it.animateText("A game by Sam and Kenneth!") }
        val gameButton = findViewById<Typewriter>(R.id.homePageGameButton).also {
            it.setDelay(50)
            it.animateText("New Game")
        }
        val leaderboardButton = findViewById<Typewriter>(R.id.homePageLeaderboardButton).also {
            it.setDelay(50)
            it.animateText("Leaderboard")
        }
        val profilePageButton = findViewById<Typewriter>(R.id.homePageProfilePageButton).also {
            it.setDelay(50)
            it.animateText("Profile")
        }
        val encyclopediaButton = findViewById<Typewriter>(R.id.homePageEncyclopediaButton).also {
            it.setDelay(50)
            it.animateText("Encyclopedia")
        }

        val continueButton = findViewById<Typewriter>(R.id.homePageContinueButton).also {
            it.setDelay(50)
            it.animateText("Resume Game")
            if (!sharedPrefs.contains("roundNumber")) {
                Log.i("MYDEBUG", "GONE")
                it.visibility = View.GONE
            }
        }

        gameButton.setOnClickListener {
            val editor = sharedPrefs.edit()
            for(key in arrayOf("heartsRemaining", "instrList", "gameTaskId", "instrR", "instrIJ", "roundNumber")) {
                editor.remove(key)
            }
            editor.commit()

            val myIntent = Intent(
                this,
                TransitionGameActivity::class.java
            )
            myIntent.putExtra("roundNumber", 1)
            startActivity(myIntent)
        }

        continueButton.setOnClickListener {
            val myIntent = Intent(
                this,
                TransitionGameActivity::class.java
            )
            myIntent.putExtra("roundNumber", sharedPrefs.getInt("roundNumber", 1))
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

        encyclopediaButton.setOnClickListener {
            val myIntent = Intent(
                this,
                Encyclopedia::class.java
            )
            startActivity(myIntent)
        }
    }
}