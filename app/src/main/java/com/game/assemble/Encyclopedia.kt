package com.game.assemble

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Encyclopedia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encyclopedia)

        // construct encyclopedia dataset
        val dataSet = mutableListOf<EncyclopediaItem>()
        val names = resources.getStringArray(R.array.instrString)
        val descriptions = resources.getStringArray(R.array.instrDescription)
        val usages = resources.getStringArray(R.array.instrUsage)

        val zippedOpcodes = names.zip(descriptions).zip(usages) {
            (a, b), c -> Triple(a, b, c)
        }

        for((name, description, usage) in zippedOpcodes) {
            dataSet.add(EncyclopediaItem(name, description, usage))
        }

        val recyclerView: RecyclerView = findViewById(R.id.encyclopediaList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = EncyclopediaRecyclerViewAdapter(dataSet.toTypedArray())
        recyclerView.adapter = customAdapter
    }
}