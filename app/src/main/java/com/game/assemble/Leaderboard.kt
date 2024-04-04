package com.game.assemble

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Leaderboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val leaderboarditem1 = LeaderboardItem("1st", "cabbit", 12);
        val leaderboarditem2 = LeaderboardItem("2nd", "xabbit", 2);
        val leaderboarditem3 = LeaderboardItem("3rd", "blabbit", 1);
        val dataset = arrayOf(leaderboarditem1, leaderboarditem2, leaderboarditem3);

        val recyclerView: RecyclerView = findViewById(R.id.LeaderboardList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = LeaderboardRecyclerViewAdapter(dataset)
        recyclerView.adapter = customAdapter
    }
}