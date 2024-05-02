package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransitionGameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transition_game)

        val textView = findViewById<TextView>(R.id.transitionTextView)
        val roundNumber = intent.getIntExtra("roundNumber", 1)

        val sim: MIPSSimulator = MIPSSimulator(this)
        val taskDescription = sim.generateTask()
        val id : Int = sim.gameTask.info["id"].toString().toInt()

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

    }
}