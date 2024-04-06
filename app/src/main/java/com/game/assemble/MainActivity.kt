package com.game.assemble

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.util.Log

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


        val sim: MIPSSimulator = MIPSSimulator(
            a0 = 0x4,
            sp = 0x7ffffff4
        )

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