package com.game.assemble

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
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.internal.notify
import org.w3c.dom.Text

class GameActivity : AppCompatActivity() {

    companion object {
        var lastAccessedGameButton: TextView? = null
        var lastRunnable: Runnable? = null
        var lastAccessedGameButtonVisible : Boolean = true
        val timeout: Handler = Handler(Looper.getMainLooper())
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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // MIPSSimulator(this)
        instrList = mutableListOf()
        instrList += Instruction(arrayOf("add"))
        instrList += Instruction(arrayOf("add"))

        val recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.gameInstructionRecyclerView)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        // Set adapter for the RecyclerView
        customAdapter = GameInstructionRecyclerViewAdapter(instrList, this)
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
            arrayOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf<String>("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf<String>("1", "2", "3", "4", "5", "6", "7", "8", "9"), // TODO: dynamically set this to the number of active instruction lines
            arrayOf<String>("Panda", "Numpy", "Bunny", "Python"),
            arrayOf<String>("Panda", "Numpy", "Bunny", "Python")
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


        // handle backspace
        val backspaceButtons = arrayOf(
            findViewById<ImageButton>(R.id.backspace1),
            findViewById<ImageButton>(R.id.backspace2),
            findViewById<ImageButton>(R.id.backspace3),
            findViewById<ImageButton>(R.id.backspace4),
            findViewById<ImageButton>(R.id.backspace5),
            findViewById<ImageButton>(R.id.backspace6)
        )
        for (backspace in backspaceButtons) {
            backspace.setOnClickListener {
                if (GameActivity.lastAccessedGameButton == null) {
                    // do nothing
                }
                else {
                    val selectedButton = GameActivity.lastAccessedGameButton
                    // if empty, move back to prev
                    if (selectedButton!!.text == "_" || selectedButton.text == "") {
                        removeSelected()
                        val currentButton = selectedButton
                        val prevButton = getPrevButton(currentButton)
                        if (
                            currentButton == getSiblingButtonList(currentButton)[0]     // First item of the line
                            && instrList.size > 1   // Not the last line remaining
                            ) {  // First item of the line
                            val idx = (currentButton.parent as ViewGroup).findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text.toString().toInt() - 1
                            instrList.removeAt(idx)
                            customAdapter.notifyItemRemoved(idx)
                            customAdapter.notifyItemChanged(idx)
                        }
                        if (currentButton != prevButton) {
                            prevButton.callOnClick()
                        }
                    }
                    else if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout) { // if num, delete last digit
                        GameActivity.lastAccessedGameButton!!.text = GameActivity.lastAccessedGameButton!!.text.toString().dropLast(1)
                        if (GameActivity.lastAccessedGameButton!!.text == "") {
                            GameActivity.lastAccessedGameButton!!.text = "_"
                        }
                        // TODO: UPDATE
                    }
                    else { // else delete thing
                        GameActivity.lastAccessedGameButton!!.text = "_"
                        // TODO: UPDATE
                        if (GameActivity.lastAccessedGameButton!! == getSiblingButtonList(GameActivity.lastAccessedGameButton!!)[0]) {
                            changeInstructionOppType(GameActivity.lastAccessedGameButton!!, GameActivity.lastAccessedGameButton!!.text.toString())
                        }
                    }
                }
            }
        }

        // by default, only have the registerLayout visible
        switchKeyboardLayout(R.id.registersKeyboardLayout)
        switchKeyboardLayout(R.id.operatorKeyboardLayout)
    }

}