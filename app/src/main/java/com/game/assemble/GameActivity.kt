package com.game.assemble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val gameInstructionItem1 = InstructionItem("ADD", "532", "782")
        val gameInstructionItem2 = InstructionItem("RAWR", "XD", "D:", "BNNUY")
        val dataSet= arrayOf(gameInstructionItem1, gameInstructionItem2)

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = GameInstructionRecyclerViewAdapter(dataSet)
        recyclerView.adapter = customAdapter

        val keyboardView = findViewById<LinearLayout>(R.id.gameInstructionKeyboardLayout)
        keyboardView.visibility = View.GONE

        val register1 = RegisterItem("a22", 6969)
        val register2 = RegisterItem("a1", 1234)
        val registerDataset = arrayOf(register2, register1, register2, register1, register2)

        val registerRecyclerView: RecyclerView = findViewById(R.id.gameInstructionRegisterRecyclerView)
        val registerLayoutManager = LinearLayoutManager(this)
        registerRecyclerView.layoutManager = registerLayoutManager

        val registerCustomAdapter = GameRegisterRecyclerViewAdapter(registerDataset)
        registerRecyclerView.adapter = registerCustomAdapter

        Log.i("item count", GameRegisterRecyclerViewAdapter(registerDataset).getItemCount().toString())
    }
}