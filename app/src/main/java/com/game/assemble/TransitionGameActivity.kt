package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransitionGameActivity : AppCompatActivity() {
    private var roundNumber: Int = 0
    private var id: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition_game)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        roundNumber = intent.getIntExtra("roundNumber", -1)
        if (roundNumber == -1) roundNumber = sharedPrefs.getInt("roundNumber", 1)

        val textView = findViewById<TextView>(R.id.transitionTextView)

        val sim: MIPSSimulator = MIPSSimulator(this)
        var taskDescription = ""

        if (sharedPrefs.contains("id")) {
            id = sharedPrefs.getInt("id", 0)
            sim.gameTask.setTask(id)
            sim.generateTask(id)

            taskDescription = sim.gameTask.info["text"] as String
        }
        else {
            id = sim.gameTask.getRandomTask()
            taskDescription = sim.gameTask.info["text"] as String
            Log.i("MYDEBUG", "id is $id")
            sim.generateTask(id)
        }

        textView.text = "GET READY FOR ROUND $roundNumber\nYOUR GOAL IS $taskDescription"

        val gameIntent = Intent(this, GameActivity::class.java)
        gameIntent.putExtra("taskId", id)
        gameIntent.putExtra("roundNumber", roundNumber)

        val readyButton = findViewById<Button>(R.id.transitionButton)
        readyButton.setOnClickListener {
            startActivity(gameIntent)
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // https://stackoverflow.com/questions/3141996/android-how-to-override-the-back-button-so-it-doesnt-finish-my-activity
                startActivity(Intent(this@TransitionGameActivity,MainActivity::class.java))
                finishAffinity()
            }
        })

        Log.i("THIS IS A LOG CALL", getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("instrList", "NOTHING SAVED")!!)
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("roundNumber", roundNumber)
        editor.putInt("gameTaskId", id)
        editor.apply()
    }
}