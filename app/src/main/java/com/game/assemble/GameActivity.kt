package com.game.assemble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameActivity : AppCompatActivity() {
    companion object {
        var lastAccessedGameButton: Button? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gameInstructionItem1 = InstructionItem("ADD", "532", "782")
        val gameInstructionItem2 = InstructionItem("RAWR", "XD", "D:", "BNNUY")
        val dataSet= arrayOf(gameInstructionItem1, gameInstructionItem2, gameInstructionItem1, gameInstructionItem2, gameInstructionItem1)

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = GameInstructionRecyclerViewAdapter(dataSet)
        recyclerView.adapter = customAdapter

        val keyboardView = findViewById<LinearLayout>(R.id.gameInstructionKeyboardLayout)
        //keyboardView.visibility = View.GONE

        val register1 = RegisterItem("a22", 6969)
        val register2 = RegisterItem("a1", 1234)
        val registerDataset = arrayOf(register2, register1, register2, register1, register2)

        val registerRecyclerView: RecyclerView = findViewById(R.id.gameInstructionRegisterRecyclerView)
        val registerLayoutManager = LinearLayoutManager(this)
        registerRecyclerView.layoutManager = registerLayoutManager

        val registerCustomAdapter = GameRegisterRecyclerViewAdapter(registerDataset)
        registerRecyclerView.adapter = registerCustomAdapter


        val keyboardTabButtons = arrayOf<Button>(
            findViewById<Button>(R.id.buttonR),
            findViewById<Button>(R.id.buttonJ),
            findViewById<Button>(R.id.buttonI),
            findViewById<Button>(R.id.buttonLabels)
        )
        val keyboardLayouts = arrayOf<GridView>(
            findViewById<GridView>(R.id.keyboardRGridView),
            findViewById<GridView>(R.id.keyboardJGridView),
            findViewById<GridView>(R.id.keyboardIGridView),
            findViewById<GridView>(R.id.keyboardLabelsGridView)
        )

        keyboardLayouts[1].visibility = View.GONE
        keyboardLayouts[2].visibility = View.GONE
        keyboardLayouts[3].visibility = View.GONE

        val keysR = listOf("R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8")
        val keysJ = listOf("J1", "J2", "J3", "J4", "J5", "J6", "J7", "J8")
        val keysI = listOf("I1", "I2", "I3", "I4", "I5", "I6", "I7", "I8")
        val keysLabels = listOf("Lab1", "Lab2", "Lab3", "Lab4", "Lab5", "Lab6", "Lab7", "Lab8")
        val keys = arrayOf(keysR, keysJ, keysI, keysLabels)

        for(i in 0 until 4) {
            val gridView: GridView = keyboardLayouts[i]
            val gridViewAdapter = KeyboardGridViewAdapter(this, keys[i])
            gridView.adapter = gridViewAdapter

            val tabButton = keyboardTabButtons[i]
            tabButton.setOnClickListener {
                for (j in 0 until 4) {
                    val gridView: GridView = keyboardLayouts[j]
                    if (i == j) {
                        gridView.visibility = View.VISIBLE
                    }
                    else {
                        gridView.visibility = View.GONE
                    }
                }
            }
        }

        val gridView: GridView = findViewById(R.id.keyboardRGridView)
        val gridViewAdapter = KeyboardGridViewAdapter(this, keysR)
        gridView.adapter = gridViewAdapter

    }
}