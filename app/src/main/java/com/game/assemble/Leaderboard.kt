package com.game.assemble

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okio.IOException

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

        // TODO: REMOVE
        submitScore("YetAnotherUsername", 2)
    }

    // from https://stackoverflow.com/questions/66059143/how-to-make-a-http-post-request-in-kotlin-android-to-simple-server
    fun submitScore(username: String, score: Int) {
        // TODO: Replace in Prod
        var url = "https://cfa9-219-79-67-13.ngrok-free.app/leaderboard.php"

        // add parameter
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("score", score.toString())
            .build()

        // creating request
        var request = Request.Builder().url(url)
            .post(formBody)
            .build()

        var client = OkHttpClient();
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.i("response", response.body!!.string())

            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i("onFailure", e.message.toString())
            }
        })
    }

}