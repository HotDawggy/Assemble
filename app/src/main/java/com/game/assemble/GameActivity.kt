package com.game.assemble

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GameActivity : AppCompatActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        var lastAccessedGameButton: TextView? = null
        private var lastRunnable: Runnable? = null
        private var lastAccessedGameButtonVisible : Boolean = true
        private val timeout: Handler = Handler(Looper.getMainLooper())
        lateinit var currentTask: String
        lateinit var keyboardLayouts: Array<LinearLayout>
        lateinit var instrList: MutableList<Instruction>
        lateinit var customAdapter: GameInstructionRecyclerViewAdapter
        lateinit var recyclerView: RecyclerView
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
        fun removeSelected() {
            if (lastAccessedGameButton != null) {
                lastAccessedGameButton!!.setTextColor(lastAccessedGameButton!!.context.getColor(R.color.code_font_color)) // Reset color
                lastAccessedGameButton!!.setTypeface(ResourcesCompat.getFont(lastAccessedGameButton!!.context, R.font.consolas)) // Unbold text
                timeout.removeCallbacks(lastRunnable!!)
                lastAccessedGameButtonVisible = true
                lastAccessedGameButton = null
            }
        }

        fun addSelected(button:TextView) {
            lastAccessedGameButton = button
            button.setTextColor(button.context.getColor(R.color.code_font_selected_color))
            lastRunnable = Runnable {
                lastAccessedGameButtonVisible = if (lastAccessedGameButtonVisible) {
                    button.setTextColor(button.textColors.withAlpha(0))
                    false
                } else {
                    button.setTextColor(button.textColors.withAlpha(255))
                    true
                }
                timeout.postDelayed(lastRunnable!!, 350)
            }
            timeout.postDelayed(lastRunnable!!, 350)
        }

        fun update() {
            // note: we have customAdapter and recyclerView
            for(i in 0 until recyclerView.childCount) {
                val layout: View = recyclerView.getChildAt(i)
                val buttons = arrayOf(
                    layout.findViewById<TextView>(R.id.gameInstructionTextView1),
                    layout.findViewById(R.id.gameInstructionTextView3),
                    layout.findViewById(R.id.gameInstructionTextView5),
                    layout.findViewById(R.id.gameInstructionTextView7)
                )
                customAdapter.updateItemAtPosition(i, Instruction(arrayOf(
                    buttons[0].text.toString().removePrefix("\t"),
                    buttons[1].text.toString(),
                    buttons[2].text.toString(),
                    buttons[3].text.toString()
                )))
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val sim = MIPSSimulator(this)
        // TODO: Deal with load game case
        Log.i("GameActivity", "Calling generateTask()")
        currentTask = sim.generateTask()
        Log.i("GameActivity", "Returned from generateTask()")
        Log.i("GameActivity", currentTask)
        instrList = mutableListOf(Instruction((arrayOf("main:"))))
        instrList += Instruction(arrayOf("add", "_", "_", "_"))
        instrList += Instruction(arrayOf("add", "_", "_", "_"))

        recyclerView = findViewById(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        customAdapter = GameInstructionRecyclerViewAdapter(instrList)
        recyclerView.adapter = customAdapter

        // initialize keyboard layouts
        keyboardLayouts = arrayOf(
            findViewById(R.id.shamtDigitKeyboardLayout),
            findViewById(R.id.immedDigitKeyboardLayout),
            findViewById(R.id.operatorKeyboardLayout),
            findViewById(R.id.lineNumberKeyboardLayout),
            findViewById(R.id.registersKeyboardLayout),
            findViewById(R.id.registers2KeyboardLayout)
        )

        val keyboardData = arrayOf<Array<String>>(
            resources.getStringArray(R.array.instr_r),
            resources.getStringArray(R.array.instr_i) + resources.getStringArray(R.array.instr_j),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9"), // TODO: dynamically set this to the number of active instruction lines
            arrayOf("Panda", "Numpy", "Bunny", "Python"),
            arrayOf("Panda", "Numpy", "Bunny", "Python")
        )

        val gridViews = arrayOf<GridView>(
            findViewById(R.id.keyboardRGridView),
            findViewById(R.id.keyboardIGridView),
            findViewById(R.id.keyboardShamtDigitsGridView),
            findViewById(R.id.keyboardImmedDigitsGridView),
            findViewById(R.id.lineNumberGridView),
            findViewById(R.id.labelsGridView),
            findViewById(R.id.keyboardLabelsGridView),
        )

        for ((data, gridView) in keyboardData.zip(gridViews)) {
            gridView.adapter = KeyboardGridViewAdapter(this, data.toList())
        }

        // setup register view for keyboard
        val keyboardRecyclerView: RecyclerView = findViewById(R.id.gameInstructionRegisterRecyclerView)
        keyboardRecyclerView.layoutManager = LinearLayoutManager(this)
        /*
        val registerArray: MutableList<RegisterItem> = mutableListOf()
        for(name in resources.getStringArray(R.array.regsString)) {
            registerArray.add(RegisterItem(name, 0))
        }*/
        keyboardRecyclerView.adapter = GameRegisterRecyclerViewAdapter(sim.regs.getMap())

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


        // handle backspace
        val backspaceButtons = arrayOf(
            findViewById(R.id.backspace1),
            findViewById(R.id.backspace2),
            findViewById(R.id.backspace3),
            findViewById(R.id.backspace4),
            findViewById(R.id.backspace5),
            findViewById<ImageButton>(R.id.backspace6)
        )
        for (backspace in backspaceButtons) {
            backspace.setOnClickListener {
                if (lastAccessedGameButton == null) {
                    // do nothing
                }
                else {
                    val selectedButton = lastAccessedGameButton
                    // if empty, move back to prev
                    if (selectedButton!!.text == "_" || selectedButton.text == "") {
                        removeSelected()
                        val prevButton = getPrevButton(selectedButton)
                        update()
                        if (
                            selectedButton == getSiblingButtonList(selectedButton)[0]     // First item of the line
                            && instrList.size > 2   // Not the last line remaining, excluding main:
                            ) {
                            val idx = (selectedButton.parent as ViewGroup).findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text.toString().toInt() - 1
                            instrList.removeAt(idx)
                            customAdapter.notifyDataSetChanged()
                        }
                        if (selectedButton != prevButton) {
                            prevButton.callOnClick()
                        }
                    }
                    else if (getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout
                        || getVisibleKeyboardLayout() == R.id.immedDigitKeyboardLayout) { // if num, delete last digit
                        lastAccessedGameButton!!.text = lastAccessedGameButton!!.text.toString().dropLast(1)
                        if (lastAccessedGameButton!!.text == "") {
                            lastAccessedGameButton!!.text = "_"
                        }
                    }
                    else { // else delete thing
                        lastAccessedGameButton!!.text = "_"
                        // UPDATE
                        if (lastAccessedGameButton!! == getSiblingButtonList(lastAccessedGameButton!!)[0]) {
                            changeInstructionOppType(lastAccessedGameButton!!, lastAccessedGameButton!!.text.toString())
                        }
                        update()
                    }
                }
            }
        }

        val runButton: ImageButton = findViewById(R.id.gamePlayButton)
        runButton.setOnClickListener {
            Log.i("runButton", "OnClick!")
            Log.i("runButton", "Updating instrList")
            update()
            if (instrList.any { it.hasNull() }) {
                Log.i("runButton", "Some fields are empty!");
                return@setOnClickListener
            }
            Log.i("runButton", "Calling validateTask()")
            val res = sim.validateTask(instrList)
            Log.i("runButton", "Returned from validateTask()")
            Log.i("runButton", "res = $res")
        }

        // by default, only have the registerLayout visible
        switchKeyboardLayout(R.id.registersKeyboardLayout)
        // switchKeyboardLayout(R.id.operatorKeyboardLayout)
    }
}