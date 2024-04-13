package com.game.assemble

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameActivity : AppCompatActivity() {

    companion object {
        var lastAccessedGameButton: TextView? = null
        lateinit var keyboardLayouts: Array<LinearLayout>

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // initialize keyboard layouts
        keyboardLayouts = arrayOf(
            findViewById(R.id.digitsKeyboardLayout),
            findViewById(R.id.operatorKeyboardLayout),
            findViewById(R.id.lineNumberKeyboardLayout),
            findViewById(R.id.gameInstructionRegisterLayout2)
        )

        Instruction.initLookup(this.applicationContext)
        var instrList = arrayOf<Instruction>()
        instrList += Instruction(opcode = 0x28, rt=4, rs=2, immediate=4)

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = GameInstructionRecyclerViewAdapter(instrList, this.applicationContext)
        recyclerView.adapter = customAdapter

        val keyboardView = findViewById<LinearLayout>(R.id.operatorKeyboardLayout)
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

        val keysR = listOf("ADD", "R2", "R3", "R4", "R5", "R6", "R7", "R8")
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


        val keyboardOps = findViewById<LinearLayout>(R.id.operatorKeyboardLayout)
        val keyboardDigits = findViewById<LinearLayout>(R.id.digitsKeyboardLayout)
        val keyboardLineNumbers = findViewById<LinearLayout>(R.id.lineNumberKeyboardLayout)

        val digitKeyboard = findViewById<GridView>(R.id.keyboardDigitsGridView)
        val keysDigits = listOf("0", "1", "2", "3", "4", "etc")
        digitKeyboard.adapter = KeyboardGridViewAdapter(this, keysDigits)

        val lineNumberKeyboard = findViewById<GridView>(R.id.lineNumberGridView)
        val keysLineNumbers = listOf("Line 1", "Line 2", "etc")
        lineNumberKeyboard.adapter = KeyboardGridViewAdapter(this, keysLineNumbers)


        //keyboardOps.visibility = View.GONE
        keyboardDigits.visibility = View.GONE
        keyboardLineNumbers.visibility = View.GONE
    }
}