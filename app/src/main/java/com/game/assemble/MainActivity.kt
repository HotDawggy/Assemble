package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        encyclopediaButton.setOnClickListener {
            val myIntent = Intent(
                this,
                Encyclopedia::class.java
            )
            startActivity(myIntent)
        }


        // Testing MIPSSimulator, remove on production
        /*
        sim.stack = byteArrayOf(0x10, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x20)
        sim.Run(listOf(
            Instruction(opcode = 0x28, rt = 4, rs = 29, immediate = 4)
            //Instruction(),
            //Instruction(opcode = 0, rd = 2, rs = 4, rt = 5, funct = 0x20)
        ))
        if (sim.err.isNotBlank()) {
            Log.i("sim.err", sim.err)
            sim.printState()
        }
        else {
            Log.i("sim.v0", sim.regs[2].toString())
            sim.printStack()
        }
         */
    }
}