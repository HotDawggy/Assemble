package com.game.assemble

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Task : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        val task_play_button = findViewById<ImageButton>(R.id.taskPlayButton)
        val task_title = findViewById<TextView>(R.id.taskTitleTextView)
        val task_description = findViewById<TextView>(R.id.taskDescriptionTextView)

        task_title.text = "Round 1"
        task_description.text = "Task(s):\nWrite the LCM of the 2 given numbers (in \$a0, \$a1) to \$v0"

        task_play_button.setOnClickListener {
            val myIntent = Intent(
                this,
                GameActivity::class.java
            )
            startActivity(myIntent)
        }
    }
}