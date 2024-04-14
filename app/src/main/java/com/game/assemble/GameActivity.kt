package com.game.assemble

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameActivity : AppCompatActivity() {

    companion object {
        var lastAccessedGameButton: TextView? = null
        lateinit var keyboardLayouts: Array<LinearLayout>
        lateinit var instrList: MutableList<Instruction>
        lateinit var customAdapter: GameInstructionRecyclerViewAdapter
        fun switchKeyboardLayout(selectedLayoutId: Int) {
            for (layout in keyboardLayouts) {
                if (layout.id == selectedLayoutId) {
                    layout.visibility = View.VISIBLE
                } else {
                    layout.visibility = View.GONE
                }
            }
        }

        fun getVisibleKeyboardLayout(): Int {
            for (layout in keyboardLayouts) {
                if (layout.isVisible) {
                    return layout.id
                }
            }
            return 0 // should never reach here
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        MIPSSimulator(this)
        instrList = mutableListOf()
        instrList += Instruction(opcode = 0x28, rt=4, rs=2, immediate=4)

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        customAdapter = GameInstructionRecyclerViewAdapter(instrList.toTypedArray(), this)
        recyclerView.adapter = customAdapter

        // initialize keyboard layouts
        keyboardLayouts = arrayOf(
            findViewById(R.id.digitsKeyboardLayout),
            findViewById(R.id.operatorKeyboardLayout),
            findViewById(R.id.lineNumberKeyboardLayout),
            findViewById(R.id.gameInstructionRegisterLayout2)
        )

        val keyboardData = arrayOf<Array<String>>(
            resources.getStringArray(R.array.instr_r),
            resources.getStringArray(R.array.instr_i) + resources.getStringArray(R.array.instr_j),
            arrayOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9"), // TODO: dynamically set this to the number of active instruction lines
            arrayOf<String>("Panda", "Numpy", "Bunny", "Python"),
            arrayOf<String>("Panda", "Numpy", "Bunny", "Python")
        )

        val gridViews = arrayOf<GridView>(
            findViewById(R.id.keyboardRGridView),
            findViewById(R.id.keyboardIGridView),
            findViewById(R.id.keyboardDigitsGridView),
            findViewById(R.id.lineNumberGridView),
            findViewById(R.id.labelsGridView),
            findViewById(R.id.keyboardLabelsGridView)
        )

        for ((data, gridView) in keyboardData.zip(gridViews)) {
            gridView.adapter = KeyboardGridViewAdapter(this, data.toList())
        }

        // setup register view for keyboard
        val keyboardRecyclerView: RecyclerView = findViewById(R.id.gameInstructionRegisterRecyclerView)
        keyboardRecyclerView.layoutManager = LinearLayoutManager(this)
        var registerArray: MutableList<RegisterItem> = mutableListOf()
        for(name in resources.getStringArray(R.array.regsString)) {
            registerArray.add(RegisterItem(name, 0))
        }
        keyboardRecyclerView.adapter = GameRegisterRecyclerViewAdapter(registerArray.toTypedArray())

        // setup the operator tab buttons (switch between R-type and J/I-type ops)
        val buttonR: Button = findViewById(R.id.buttonR)
        val buttonI: Button = findViewById(R.id.buttonI)
        val buttonL: Button = findViewById(R.id.buttonL)

        val buttonLineNumber: Button = findViewById(R.id.buttonLineNumber)
        val buttonLabels: Button = findViewById(R.id.buttonLabels)

        buttonR.setOnClickListener {
            gridViews[0].visibility = View.VISIBLE
            gridViews[1].visibility = View.GONE
            gridViews[5].visibility = View.GONE
        }
        buttonI.setOnClickListener {
            gridViews[0].visibility = View.GONE
            gridViews[1].visibility = View.VISIBLE
            gridViews[5].visibility = View.GONE
        }
        buttonL.setOnClickListener {
            gridViews[0].visibility = View.GONE
            gridViews[1].visibility = View.GONE
            gridViews[5].visibility = View.VISIBLE
        }

        buttonLineNumber.setOnClickListener {
            gridViews[3].visibility = View.VISIBLE
            gridViews[4].visibility = View.GONE
        }
        buttonLabels.setOnClickListener {
            gridViews[3].visibility = View.GONE
            gridViews[4].visibility = View.VISIBLE
        }

        // by default, only have the registerLayout visible
        switchKeyboardLayout(R.id.gameInstructionRegisterLayout2)
        switchKeyboardLayout(R.id.operatorKeyboardLayout)
    }
}