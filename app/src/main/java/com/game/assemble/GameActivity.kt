package com.game.assemble

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
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
import kotlinx.coroutines.delay

class GameActivity : AppCompatActivity() {

    private var round = 1
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
        lateinit var gridViews: Array<GridView>
        lateinit var instructionLinearLayout: LinearLayout
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
                lastAccessedGameButton!!.setBackgroundResource(R.color.theme)
                lastAccessedGameButton!!.setBackgroundTintList(lastAccessedGameButton!!.context.getColorStateList(R.color.theme).withAlpha(0))
                (lastAccessedGameButton!!.parent as LinearLayout).setBackgroundResource(R.color.theme)
                (lastAccessedGameButton!!.parent as LinearLayout).setBackgroundTintList(lastAccessedGameButton!!.context.getColorStateList(R.color.theme))
                lastAccessedGameButton!!.setTextColor(lastAccessedGameButton!!.textColors.withAlpha(255)) // Reset color
                lastAccessedGameButton!!.setTypeface(ResourcesCompat.getFont(lastAccessedGameButton!!.context, R.font.consolas)) // Unbold text
                timeout.removeCallbacks(lastRunnable!!)
                lastAccessedGameButtonVisible = true
                lastAccessedGameButton = null
            }
        }

        fun addSelected(button:TextView) {
            lastAccessedGameButton = button
            button.setBackgroundResource(R.color.theme_selected)
            button.setBackgroundTintList(button.context.getColorStateList(R.color.theme_selected))
            (button.parent as LinearLayout).setBackgroundResource(R.color.theme4)
            (button.parent as LinearLayout).setBackgroundTintList(lastAccessedGameButton!!.context.getColorStateList(R.color.theme4))
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

        fun update() { // fix line number; instrList <- view text
            for(i in 0 until instrList.size) {
                val buttons = arrayOf<TextView>(
                    instructionLinearLayout.getChildAt(i).findViewById(R.id.gameInstructionTextView1),
                    instructionLinearLayout.getChildAt(i).findViewById(R.id.gameInstructionTextView3),
                    instructionLinearLayout.getChildAt(i).findViewById(R.id.gameInstructionTextView5),
                    instructionLinearLayout.getChildAt(i).findViewById(R.id.gameInstructionTextView7)
                )

                instructionLinearLayout.getChildAt(i).findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text = (i + 1).toString()
                instrList[i] = Instruction(arrayOf<String?>(
                    buttons[0].text.toString().removeSuffix(":").removePrefix("\t"),
                    buttons[1].text.toString().removePrefix("\t"),
                    buttons[2].text.toString().removePrefix("\t"),
                    buttons[3].text.toString().removePrefix("\t")
                ))
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val sim = MIPSSimulator(this)
        // TODO: Deal with load game case
        //Log.i("GameActivity", "Calling generateTask()")
        currentTask = sim.generateTask()
        //Log.i("GameActivity", "Returned from generateTask()")
        //Log.i("GameActivity", currentTask)
        instrList = mutableListOf(Instruction((arrayOf("main:"))))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        //instrList += Instruction(arrayOf("add", "_", "_", "_"))
        instrList += Instruction(arrayOf("and", "\$v0", "\$v0", "\$zero"))
        instrList += Instruction(arrayOf("multiply:"))
        instrList += Instruction(arrayOf("add", "\$v0", "\$v0", "\$a0"))
        instrList += Instruction(arrayOf("addi", "\$a1", "\$a1", "-1"))
        instrList += Instruction(arrayOf("bne", "\$a1", "\$zero", "multiply"))
        instrList += Instruction(arrayOf("j", "exit"))


        instructionLinearLayout = findViewById<LinearLayout>(R.id.gameInstructionLinearLayout)
        instrList.forEachIndexed { index, instruction ->
            val view = LayoutInflater.from(this).inflate(R.layout.game_instruction_item, null)
            this.modifyView(view, index, instruction)
            instructionLinearLayout.addView(view)
        }

        // initialize keyboard layouts
        keyboardLayouts = arrayOf(
            findViewById(R.id.shamtDigitKeyboardLayout),
            findViewById(R.id.immedDigitKeyboardLayout),
            findViewById(R.id.operatorKeyboardLayout),
            findViewById(R.id.labelsKeyboardLayout),
            findViewById(R.id.registersKeyboardLayout),
            findViewById(R.id.registers2KeyboardLayout)
        )

        val keyboardData = arrayOf<Array<String>>(
            resources.getStringArray(R.array.instr_r),
            resources.getStringArray(R.array.instr_i) + resources.getStringArray(R.array.instr_j),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-"),
            arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9"), // TODO: dynamically set this to the number of active instruction lines
            resources.getStringArray(R.array.label_names),
            resources.getStringArray(R.array.label_names)
        )

        gridViews = arrayOf<GridView>(
            findViewById(R.id.keyboardRGridView),
            findViewById(R.id.keyboardIGridView),
            findViewById(R.id.keyboardShamtDigitsGridView),
            findViewById(R.id.keyboardImmedDigitsGridView),
            findViewById(R.id.labelsGridView),
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

        val keyboardRecyclerView2: RecyclerView = findViewById(R.id.gameInstructionRegister2RecyclerView)
        keyboardRecyclerView2.layoutManager = LinearLayoutManager(this)
        keyboardRecyclerView2.adapter = GameRegisterRecyclerViewAdapter(sim.regs.getMap())

        // setup the operator tab buttons (switch between R-type and J/I-type ops)
        val buttonR: Button = findViewById(R.id.buttonR)
        val buttonI: Button = findViewById(R.id.buttonI)
        val buttonL: Button = findViewById(R.id.buttonL)

        val buttonLabels: Button = findViewById(R.id.buttonLabels)

        buttonR.setOnClickListener {
            findViewById<GridView>(R.id.keyboardRGridView).visibility = View.VISIBLE
            findViewById<GridView>(R.id.keyboardIGridView).visibility = View.GONE
            findViewById<GridView>(R.id.keyboardLabelsGridView).visibility = View.GONE
        }
        buttonI.setOnClickListener {
            findViewById<GridView>(R.id.keyboardRGridView).visibility = View.GONE
            findViewById<GridView>(R.id.keyboardIGridView).visibility = View.VISIBLE
            findViewById<GridView>(R.id.keyboardLabelsGridView).visibility = View.GONE
        }
        buttonL.setOnClickListener {
            findViewById<GridView>(R.id.keyboardRGridView).visibility = View.GONE
            findViewById<GridView>(R.id.keyboardIGridView).visibility = View.GONE
            findViewById<GridView>(R.id.keyboardLabelsGridView).visibility = View.VISIBLE
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
                        var prevButton = getPrevButton(selectedButton)
                        // if first item of the line => get prev button and highlight it
                        if (selectedButton == getSiblingButtonList(selectedButton)[0] && selectedButton != prevButton) {
                            val idx = (selectedButton.parent as ViewGroup).findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text.toString().toInt() - 1
                            if (idx > 0) {
                                Log.i("index is", idx.toString())
                                instrList.removeAt(idx)
                                instructionLinearLayout.removeViewAt(idx)
                                update()

                                lastAccessedGameButton = null
                                var prevButton: TextView? = null
                                val buttons = arrayOf<TextView>(
                                    instructionLinearLayout.getChildAt(idx - 1).findViewById(R.id.gameInstructionTextView1),
                                    instructionLinearLayout.getChildAt(idx - 1).findViewById(R.id.gameInstructionTextView3),
                                    instructionLinearLayout.getChildAt(idx - 1).findViewById(R.id.gameInstructionTextView5),
                                    instructionLinearLayout.getChildAt(idx - 1).findViewById(R.id.gameInstructionTextView7),
                                )
                                for(button in buttons) {
                                    if (button.visibility == View.VISIBLE) {
                                        prevButton = button
                                    }
                                }

                                if (prevButton != null) {
                                    Log.i("calling" , "IS ON CLICK")
                                    prevButton!!.callOnClick()
                                }
                            }
                        }
                        else if (selectedButton != prevButton) {
                            prevButton.callOnClick()
                        }
                    }
                    else if (getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout
                        || getVisibleKeyboardLayout() == R.id.immedDigitKeyboardLayout) { // if num, delete last digit
                        lastAccessedGameButton!!.text = lastAccessedGameButton!!.text.toString().dropLast(1)
                        if (lastAccessedGameButton!!.text == "") {
                            lastAccessedGameButton!!.text = "_"
                            lastAccessedGameButton!!.setTextColor(lastAccessedGameButton!!.context.getColor(R.color.code))
                        }
                    }
                    else if (lastAccessedGameButton!!.text.toString() == "main:") {
                        // do nothing
                    }
                    else { // else delete thing
                        lastAccessedGameButton!!.text = "_"
                        if (getSiblingButtonList(lastAccessedGameButton!!)[0] == lastAccessedGameButton) {
                            getSiblingButtonList(lastAccessedGameButton!!).forEach {
                                it.setTextColor(it.context.getColor(R.color.code))
                            }
                        } else {
                            lastAccessedGameButton!!.setTextColor(lastAccessedGameButton!!.context.getColor(R.color.code))
                        }
                        // UPDATE
                        if (lastAccessedGameButton!! == getSiblingButtonList(lastAccessedGameButton!!)[0]) {
                            changeInstructionOppType(lastAccessedGameButton!!, lastAccessedGameButton!!.text.toString())
                        }
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.gameInfoExit).setOnClickListener {
            this.findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.GONE
            this.findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.VISIBLE
        }
        val runButton: ImageButton = findViewById(R.id.gamePlayButton)
        runButton.setOnClickListener {
            val infoTypewriter = this.findViewById<Typewriter>(R.id.gameInfoTypewriter)
            Log.i("runButton", "OnClick!")
            Log.i("runButton", "Updating instrList")
            update()
            infoTypewriter.clearText()
            if (instrList.any { it.hasNull() }) {
                val msg = "Error:\nSome fields are empty!"
                Log.i("runButton", msg);
                this.findViewById<TextView>(R.id.gameInfoTitleTextView).text = "Running"
                this.findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.VISIBLE
                infoTypewriter.animateText(msg)
                this.findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.GONE
                return@setOnClickListener
            }
            this.findViewById<TextView>(R.id.gameInfoTitleTextView).text = "Running"
            this.findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.VISIBLE
            this.findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.GONE
            Log.i("runButton", "Calling validateTask()")
            var res = sim.validateTask(instrList)
            var code = res[0]
            var msg =  res[1]
            if (res[0] != "Success!") {
                infoTypewriter.appendText("Known test case: $code\n")
                infoTypewriter.appendText("$msg\n")
                return@setOnClickListener
            } else {
                infoTypewriter.appendText("Known test case: $code\n")
            }
            for (i in 0 until 10) {
                sim.generateTask(sim.gameTask["id"] as Int?)
                res = sim.validateTask(instrList)
                code = res[0]
                msg =  res[1]
                if (res[0] != "Success!") {
                    infoTypewriter.appendText("Hidden test case $i: $code\n")
                    infoTypewriter.appendText("$msg\n")
                    return@setOnClickListener
                } else {
                    infoTypewriter.appendText("Hidden test case $i: $code\n")
                }
            }
            Log.i("runButton", "Returned from validateTask()")
            Log.i("runButton", "res = $res")
        }

        findViewById<TextView>(R.id.gameRoundTextView).text = "Round $round"

        // by default, only have the registerLayout visible
        switchKeyboardLayout(R.id.registersKeyboardLayout)
        // switchKeyboardLayout(R.id.operatorKeyboardLayout)
    }

    fun modifyView(view: View, index: Int, instr: Instruction) {
        view.findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text = (index + 1).toString()
        view.findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).visibility = View.VISIBLE // TODO: SET THIS IN XML INSTEAD

        // set on click listener and stuff
        val opButton = view.findViewById<TextView>(R.id.gameInstructionTextView1)
        val paramButtons = arrayOf<TextView>(
            view.findViewById(R.id.gameInstructionTextView3),
            view.findViewById(R.id.gameInstructionTextView5),
            view.findViewById(R.id.gameInstructionTextView7)
        )
        if (instr[0] != "main:") {
            for (i in 0 until 4) {
                var button = opButton
                if (i == 0) {
                    if (instr.isLabel()) {
                        opButton.text = instr[0]
                        opButton.setTextColor(opButton.context.getColor(R.color.code_label))
                    } else {
                        opButton.text = "\t" + instr[0]
                        opButton.setTextColor(opButton.context.getColor(R.color.code_instr))
                    }
                } else {
                    button = paramButtons[i - 1]
                    if (i < getKeyboardFromOperator(instr[0]!!).size) {
                        val color = when(getKeyboardFromOperator(instr[0]!!)[i]) {
                            R.id.immedDigitKeyboardLayout, R.id.shamtDigitKeyboardLayout -> R.color.code_num
                            R.id.labelsKeyboardLayout -> R.color.code_label
                            else -> R.color.code
                        }
                        button.setTextColor(button.context.getColor(color))
                    }
                    if (i < instr.size) {
                        button.visibility = View.VISIBLE
                        button.text = instr[i]
                    } else {
                        button.visibility = View.INVISIBLE
                        button.text = "_"
                    }
                }
                if (button.text == "") {
                    button.text = "_"
                }

                Log.i("text is ", button.text.toString())

                button.setOnClickListener {
                    if (lastAccessedGameButton == button) {
                        removeSelected()
                        switchKeyboardLayout(R.id.registersKeyboardLayout)
                    } else {
                        removeSelected()
                        addSelected(button)
                        switchKeyboardLayout(
                            getKeyboardFromOperator(opButton.text.toString())[i]
                        )
                    }
                }
            }
        } else {
            opButton.text = instr[0]
            opButton.setTextColor(opButton.context.getColor(R.color.code_label))
            opButton.visibility = View.VISIBLE
        }

        // TODO 2 - set template
        val allButtons = arrayOf<TextView>(
            view.findViewById(R.id.gameInstructionTextView1),
            view.findViewById(R.id.gameInstructionTextView2),
            view.findViewById(R.id.gameInstructionTextView3),
            view.findViewById(R.id.gameInstructionTextView4),
            view.findViewById(R.id.gameInstructionTextView5),
            view.findViewById(R.id.gameInstructionTextView6),
            view.findViewById(R.id.gameInstructionTextView7),
            view.findViewById(R.id.gameInstructionTextView8),
        )
        if (opButton.text == "" || opButton.text == "_") {
            for (button in paramButtons) {
                button.text = "_"
                button.visibility = View.INVISIBLE
            }
        }
        else {
            val template = Instruction(arrayOf(opButton.text.toString())).getTemplateFromOperator()
            for (i in 0 until 8) {
                val currentButton = allButtons[i]
                if (i >= template.size) {
                    currentButton.visibility = View.INVISIBLE
                    currentButton.text = "_"
                }
                else if (template[i] != "_") {
                    currentButton.visibility = View.VISIBLE
                    currentButton.text = template[i]
                }
                else {
                    currentButton.visibility = View.VISIBLE
                    currentButton.text = if (currentButton.text == "") {
                        "_"
                    }
                    else {
                        currentButton.text
                    }
                }
            }
        }
    }
}