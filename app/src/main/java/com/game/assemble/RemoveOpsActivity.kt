package com.game.assemble

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.allViews
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RemoveOpsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_ops)

        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val opsR = regListToStringArray(sharedPrefs.getString("instrR", "")!!)
        val opsIJ = regListToStringArray(sharedPrefs.getString("instrIJ", "")!!)

        val allOps = opsR + opsIJ
        val n = allOps.size
        var toRemove = n / 4

        val textView = findViewById<TextView>(R.id.removeOpsTextView)
        textView.text = "Pick another $toRemove operator(s) to remove."

        val gridView = findViewById<GridView>(R.id.removeOpsGridView)
        gridView.adapter = KeyboardRemoveOpsGridViewAdapter(this, allOps.toList())

        var newOpsR = opsR.toMutableList()
        var newOpsIJ = opsIJ.toMutableList()

        var selected = mutableListOf<String>()

        gridView.setOnItemClickListener { parent, view, position, id ->
            val button = view as Button
            val op = button.text.toString()
            if (op in selected) {
                selected.remove(op)
                toRemove += 1

                if (op in opsR) newOpsR.add(op)
                else if (op in opsIJ) newOpsIJ.add(op)

                button.setTextColor(Color.WHITE)
            } else {
                selected.add(op)
                toRemove -= 1

                if (op in newOpsR) newOpsR.remove(op)
                else if (op in newOpsIJ) newOpsIJ.remove(op)

                button.setTextColor(Color.GREEN)
            }
            textView.text = "Pick another $toRemove operator(s) to remove."

            if (toRemove == 0) {
                val editor = sharedPrefs.edit()

                editor.putString("instrR", stringArrayToRegList(newOpsR.toTypedArray()))
                editor.putString("instrIJ", stringArrayToRegList(newOpsIJ.toTypedArray()))
                editor.commit()
                startActivity(Intent(this, TransitionGameActivity::class.java))
            }
        }
    }
}