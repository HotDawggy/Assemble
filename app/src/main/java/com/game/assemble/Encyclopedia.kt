package com.game.assemble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Encyclopedia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encyclopedia)

        val encyclopediaItem1 = EncyclopediaItem("ADD", "Adds registers and stuff D:")
        val encyclopediaItem2 = EncyclopediaItem("XOR", "self-explanatory")
        val dataSet = arrayOf(encyclopediaItem1, encyclopediaItem2)

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.encyclopediaList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = EncyclopediaRecyclerViewAdapter(dataSet)
        recyclerView.adapter = customAdapter
    }
}