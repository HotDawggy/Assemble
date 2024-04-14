package com.game.assemble

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONArray
import org.json.JSONObject

class Leaderboard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val dataset = arrayOf<LeaderboardItem>()

        val recyclerView: RecyclerView = findViewById(R.id.LeaderboardList)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        val customAdapter = LeaderboardRecyclerViewAdapter(dataset)
        recyclerView.adapter = customAdapter

        val dayButton: Button = findViewById(R.id.btnLeaderboardDay)
        val weekButton: Button = findViewById(R.id.btnLeaderboardWeek)
        val allButton: Button = findViewById(R.id.btnLeaderboardAll)

        dayButton.setOnClickListener {
            requestScore("day") {response ->
                val arr = parseResponse(response!!)

                runOnUiThread {
                    customAdapter.updateData(arr.toTypedArray())
                }
            }
        }
        weekButton.setOnClickListener {
            requestScore("week") {response ->
                val arr = parseResponse(response!!)

                runOnUiThread {
                    customAdapter.updateData(arr.toTypedArray())
                }
            }
        }
        allButton.setOnClickListener {
            requestScore("all") {response ->
                val arr = parseResponse(response!!)

                runOnUiThread {
                    customAdapter.updateData(arr.toTypedArray())
                }
            }
        }

        allButton.callOnClick()
    }

    fun parseResponse(response: String): List<LeaderboardItem> {
        val jsonArray = JSONArray(response)
        val leaderboardArray = mutableListOf<LeaderboardItem>()

        for(i in 0 until jsonArray.length()) {
            val jsonObject: JSONObject = jsonArray.getJSONObject(i)
            val item = LeaderboardItem((i + 1).toString() + "-th", jsonObject.getString("username"), jsonObject.getString("score").toInt())
            leaderboardArray.add(item)
        }

        return leaderboardArray
    }

    // from https://stackoverflow.com/questions/66059143/how-to-make-a-http-post-request-in-kotlin-android-to-simple-server
    fun submitScore(username: String, score: Int) {
        // TODO: Replace in Prod
        val url = "https://0553-175-159-124-117.ngrok-free.app/leaderboard.php"

        // add parameter
        val formBody = FormBody.Builder()
            .add("username", username)
            .add("score", score.toString())
            .build()

        // creating request
        val request = Request.Builder().url(url)
            .post(formBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                Log.i("response", response.body!!.string())
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i("onFailure", e.message.toString())
            }
        })
    }

    fun requestScore(timespan: String, onResponse: (String?) -> Unit) {
        // TODO: Replace in Prod
        val baseUrl = "https://0553-175-159-124-117.ngrok-free.app/leaderboard.php"

        // from https://stackoverflow.com/questions/65884020/http-get-request-with-parameters-in-okhttp-android-kotlin
        val url = baseUrl.toHttpUrl().newBuilder()
            .addQueryParameter("timespan", timespan)
            .build()

        val request = Request.Builder().url(url)
            .header("User-Agent", "OkHttp Headers.java")
            .header("Accept", "application/json")
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body!!.string()
                onResponse(body)
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i("onFailure", e.message.toString())
            }
        })
    }
}