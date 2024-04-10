package com.game.assemble

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    }
}