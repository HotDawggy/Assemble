package com.game.assemble

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

class GameActivity : AppCompatActivity() {

    private var round = 1
    companion object {
        @SuppressLint("StaticFieldLeak")
        var lastAccessedGameButton: TextView? = null
        private var lastRunnable: Runnable? = null
        private var lastAccessedGameButtonVisible : Boolean = true
        private val timeout: Handler = Handler(Looper.getMainLooper())
        lateinit var keyboardLayouts: Array<LinearLayout>
        lateinit var instrList: MutableList<Instruction>
        lateinit var gridViews: Array<GridView>
        lateinit var instructionLinearLayout: LinearLayout
        lateinit var myHelper: Helper
        lateinit var heartsRemaining: String
        lateinit var sim: MIPSSimulator
        lateinit var keyboardData: Array<Array<String>>
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
                    buttons[0].text.toString(),
                    buttons[1].text.toString(),
                    buttons[2].text.toString(),
                    buttons[3].text.toString()
                ))
            }
        }

        fun modifyView(view: View, index: Int, instr: Instruction) {
            view.findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).text = (index + 1).toString()
            view.findViewById<TextView>(R.id.gameInstructionItemLineNumberTextView).visibility = View.VISIBLE

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
                        if (i < getKeyboardFromOperator(instr[0]).size) {
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
                                getKeyboardFromOperator(opButton.text.toString().removePrefix("\t").removeSuffix(":"))[i]
                            )
                        }
                    }
                }
            } else {
                opButton.text = instr[0]
                opButton.setTextColor(opButton.context.getColor(R.color.code_label))
                opButton.visibility = View.VISIBLE
            }

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
                for (button in allButtons) {
                    button.text = ""
                    button.visibility = View.INVISIBLE
                }
                allButtons[0].text = "_"
                allButtons[0].visibility = View.VISIBLE
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        myHelper = Helper(this)

        sim = MIPSSimulator(this)
        // i can has save data
        val sharedPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        heartsRemaining = sharedPrefs.getString("heartsRemaining", "HH")!!
        instrList = stringToInstrList(sharedPrefs.getString("instrList","")!!)
        Log.i("somehowSaved", sharedPrefs.getString("instrList", "NOT ACTUALLY SAVED")!!)
        if (instrList.isEmpty()) {
            instrList += Instruction(arrayOf("main:"))
            instrList += Instruction()
        }

        if (intent.getIntExtra("roundNumber", -1) != -1) {
            round = intent.getIntExtra("roundNumber", -1)
        }
        else {
            round = sharedPrefs.getInt("roundNumber", 1)
        }

        // currentTask = sharedPrefs.getString("currentTask", sim.generateTask())!!
        if (sharedPrefs.contains("gameTaskId")) {
            val id = sharedPrefs.getInt("gameTaskId", 0)!!
            sim.gameTask.setTask(id)
            sim.generateTask(id)
        }
        else if (intent.getIntExtra("taskId", -1) != -1) {
            val id = intent.getIntExtra("taskId", -1)
            sim.gameTask.setTask(id)
            sim.generateTask(id)
        }
        else {
            val id = sim.gameTask.getRandomTask()
            sim.generateTask(id)
        }

        instructionLinearLayout = findViewById<LinearLayout>(R.id.gameInstructionLinearLayout)
        instrList.forEachIndexed { index, instruction ->
            val view = LayoutInflater.from(this).inflate(R.layout.game_instruction_item, null)
            modifyView(view, index, instruction)
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

        keyboardData = arrayOf<Array<String>>(
            resources.getStringArray(R.array.instr_r),
            resources.getStringArray(R.array.instr_i) + resources.getStringArray(R.array.instr_j),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-"),
            arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9"),
            arrayOf<String>("main", "exit") + resources.getStringArray(R.array.label_names),
            resources.getStringArray(R.array.label_names)
        )
        // load operators from keyboard
        if (sharedPrefs.contains("instrR")) {
            val instrR = regListToStringArray(sharedPrefs.getString("instrR", "")!!)
            Log.i("restoring", sharedPrefs.getString("instrR", "")!!)
            keyboardData[0] = instrR
        }
        if (sharedPrefs.contains("instrIJ")) {
            val instrIJ = regListToStringArray(sharedPrefs.getString("instrIJ", "")!!)
            keyboardData[1] = instrIJ
        }

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
        keyboardRecyclerView2.adapter = GameRegisterRecyclerViewAdapter(sim.regs.getMap2())

        for(i in 0 until instructionLinearLayout.childCount) {
            val view = instructionLinearLayout.getChildAt(i).findViewById<TextView>(R.id.gameInstructionTextView1)
            view.callOnClick()
        }

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

        // handle new lines
        val newLineButtons = arrayOf(
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton1),
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton2),
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton3),
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton4),
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton5),
            findViewById<ImageButton>(R.id.gameInstructionAddLineButton6)
        )

        for (button in newLineButtons) {
            button.setOnClickListener {
                if (GameActivity.lastAccessedGameButton != null) {
                    val parent = GameActivity.lastAccessedGameButton!!.parent as ViewGroup
                    var position = GameActivity.instructionLinearLayout.indexOfChild(parent)

                    // if operator, add new line on *next* line
                    if (GameActivity.lastAccessedGameButton != parent.findViewById(R.id.gameInstructionTextView1)) {
                        position += 1
                    }

                    val newInstr = Instruction(arrayOf("_"))
                    GameActivity.instrList.add(position, newInstr)
                    val view = LayoutInflater.from(myHelper.context)
                        .inflate(R.layout.game_instruction_item, null)
                    GameActivity.modifyView(view, position, newInstr)
                    GameActivity.instructionLinearLayout.addView(view, position)

                    update()
                }
                else {
                    // add new line on last
                    val position = GameActivity.instrList.size

                    val newInstr = Instruction(arrayOf("_"))
                    GameActivity.instrList.add(position, newInstr)
                    val view = LayoutInflater.from(myHelper.context)
                        .inflate(R.layout.game_instruction_item, null)
                    GameActivity.modifyView(view, position, newInstr)
                    GameActivity.instructionLinearLayout.addView(view, position)

                }
            }
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
                                instrList.removeAt(idx)
                                instructionLinearLayout.removeViewAt(idx)
                                update()
                                lastAccessedGameButton = null

                                if (idx > 1) {
                                    var prevButton: TextView? = null
                                    val buttons = arrayOf<TextView>(
                                        instructionLinearLayout.getChildAt(idx - 1)
                                            .findViewById(R.id.gameInstructionTextView1),
                                        instructionLinearLayout.getChildAt(idx - 1)
                                            .findViewById(R.id.gameInstructionTextView3),
                                        instructionLinearLayout.getChildAt(idx - 1)
                                            .findViewById(R.id.gameInstructionTextView5),
                                        instructionLinearLayout.getChildAt(idx - 1)
                                            .findViewById(R.id.gameInstructionTextView7),
                                    )
                                    for (button in buttons) {
                                        if (button.visibility == View.VISIBLE) {
                                            prevButton = button
                                        }
                                    }

                                    if (prevButton != null) {
                                        Log.i("calling", "IS ON CLICK")
                                        prevButton!!.callOnClick()
                                    }
                                }
                                else if (instrList.size >= 2) {
                                    // highlight the 2nd line's operator
                                    instructionLinearLayout.getChildAt(1).findViewById<TextView>(R.id.gameInstructionTextView1).callOnClick()
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
        val runButton: ImageButton = findViewById(R.id.gamePlayButton)
        val taskButton: ImageButton = findViewById(R.id.gameTaskButton)
        findViewById<ImageButton>(R.id.gameInfoExit).setOnClickListener {
            this.findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.GONE
            this.findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.VISIBLE
            runButton.isClickable = true
            taskButton.isClickable = true
        }
        val infoTypewriter: Typewriter = findViewById(R.id.gameInfoTypewriter)
        runButton.setOnClickListener {
            runButton.isClickable = false
            update()
            findViewById<ImageButton>(R.id.gameInfoExit).visibility = View.INVISIBLE
            infoTypewriter.clearText()
            this.findViewById<TextView>(R.id.gameInfoTitleTextView).text = "Test Running"
            this.findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.VISIBLE
            this.findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.GONE
            if (instrList.any { it.hasNull() }) {
                infoTypewriter.appendText("Error!\nSome fields are empty!")
                findViewById<ImageButton>(R.id.gameInfoExit).visibility = View.VISIBLE
                return@setOnClickListener
            }
            var res = ""
            lifecycleScope.launch {
                val taskID = sim.gameTask["id"] as Int
                infoTypewriter.appendText("Known test case: \n\n")
                infoTypewriter.appendText("Expected output: \n" + (sim.gameTask["goal"]).toString() + "\n\n")
                res = async {
                    withContext(Dispatchers.Default) {
                        sim.validateTask(instrList)
                    }
                }.await()
                delay(1000)

                var playerWinRound = true
                if (res != "Success!") {
                    findViewById<ImageButton>(R.id.gameInfoExit).visibility = View.VISIBLE
                    playerWinRound = false
                }
                infoTypewriter.appendText("Obtained output: \n" + (sim.gameTask["obtained"]).toString() + "\n\n")
                infoTypewriter.appendText(res + "\n")
                delay(3000);
                if (playerWinRound) {
                    for (i in 0..<10) {
                        delay(1000)
                        infoTypewriter.clearText()

                        sim = MIPSSimulator(this@GameActivity)
                        sim.generateTask(taskID)
                        infoTypewriter.appendText("Hidden test case " + (i + 1).toString() + ": \n\n")
                        infoTypewriter.appendText("Expected output: \n" + (sim.gameTask["goal"]).toString() + "\n\n")
                        res = async {
                            withContext(Dispatchers.Default) {
                                sim.validateTask(instrList)
                            }
                        }.await()
                        delay(1000)
                        infoTypewriter.appendText("Obtained output: \n" + (sim.gameTask["obtained"]).toString() + "\n\n")
                        infoTypewriter.appendText(res + "\n")
                        if (res != "Success!") {
                            playerWinRound = false
                            findViewById<ImageButton>(R.id.gameInfoExit).visibility =
                                View.VISIBLE
                            break
                        }
                        delay(2000)
                    }
                }

                delay(1000)
                findViewById<ImageButton>(R.id.gameInfoExit).visibility = View.VISIBLE
                if (playerWinRound) {
                    Log.i("playerWinRound", playerWinRound.toString())

                    // update stats (fav instr)
                    for(instruction in instrList) {
                        val op = instruction[0]!!.removePrefix("\t").removeSuffix(":")
                        var regList = mutableListOf<String>()
                        regList += resources.getStringArray(R.array.instr_r)
                        regList += resources.getStringArray(R.array.instr_i)
                        regList += resources.getStringArray(R.array.instr_j)
                        if (op in regList) {
                            val editor = sharedPrefs.edit()
                            editor.putInt(op, sharedPrefs.getInt(op, 0) + 1) // inefficient but probably fine
                            editor.commit()
                        }
                    }

                    // update high score
                    sharedPrefs.edit().putInt("highScore", max(round, sharedPrefs.getInt("highScore", 0))).apply()

                    // clear saved data
                    val editor = sharedPrefs.edit()
                    for(key in arrayOf("heartsRemaining", "instrList", "gameTaskId", "instrR", "instrIJ", "roundNumber")) {
                        editor.remove(key)
                    }
                    editor.commit()

                    editor.putString("instrR", stringArrayToRegList(keyboardData[0]))
                    editor.putString("instrIJ", stringArrayToRegList(keyboardData[1]))

                    editor.apply()

                    instrList.clear()
                    instructionLinearLayout.removeAllViews()

                    val intent = Intent(this@GameActivity, RemoveOpsActivity::class.java)
                    intent.putExtra("roundNumber", round)
                    Log.i("MYDEBUG", "putting new round number ${round}")
                    withContext(Dispatchers.Main) {
                        startActivity(intent)
                    }
                }
                else if (heartsRemaining.isNotEmpty()) {
                    Log.i("playerWinRound", heartsRemaining.toString())
                    heartsRemaining = heartsRemaining.dropLast(1)
                    findViewById<TextView>(R.id.gameInfoHeartsRemaining).text = "♥".repeat(heartsRemaining.length + 1)

                    // reset mips regs (otherwise this causes some error in sim.validateTask())
                    val taskId = sim.gameTask.info["id"] as Int
                    sim = MIPSSimulator(this@GameActivity)
                    sim.generateTask(taskId)
                }
                else {
                    Log.i("playerWinRound", "should be game over")
                    // clear out saved data
                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    for(key in arrayOf("heartsRemaining", "instrList", "gameTaskId", "instrR", "instrIJ", "roundNumber")) {
                        editor.remove(key)
                    }

                    // update games played
                    sharedPreferences.edit().putInt("gamesPlayed",
                        sharedPreferences.getInt("gamesPlayed", 0) + 1).apply()

                    // send high score - https://stackoverflow.com/questions/68791763/how-can-i-create-an-asynchronous-post-request-in-kotlin
                    val coroutineScope = CoroutineScope(Dispatchers.IO)
                    val username = sharedPreferences.getString("username", "SOME GUY")!!
                    val score = round
                    Leaderboard().submitScore(username, score)

                    // game over
                    val intent = Intent(this@GameActivity, GameOverActivity::class.java)
                    withContext(Dispatchers.Main) {
                        startActivity(intent)
                    }
                }
            }
        }
        taskButton.setOnClickListener {
            taskButton.isClickable = false
            findViewById<TextView>(R.id.gameInfoTitleTextView).text = "Task Description"
            findViewById<LinearLayout>(R.id.gameInfoLayout).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.gameMainLayout).visibility = View.GONE
            Log.i("MYDEBUG", sim.gameTask.info["text"] as String)
            infoTypewriter.animateText(sim.gameTask.info["text"] as String)
        }

        findViewById<TextView>(R.id.gameRoundTextView).text = "Round $round"
        findViewById<TextView>(R.id.gameInfoHeartsRemaining).text = "♥".repeat(heartsRemaining.length + 1)

        // by default, only have the registerLayout visible
        switchKeyboardLayout(R.id.operatorKeyboardLayout)
        // switchKeyboardLayout(R.id.operatorKeyboardLayout)


        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // https://stackoverflow.com/questions/3141996/android-how-to-override-the-back-button-so-it-doesnt-finish-my-activity
                startActivity(Intent(this@GameActivity,MainActivity::class.java))
                finishAffinity()
            }
        })

        val encyclopediaButton: ImageButton = findViewById(R.id.buttonToEncyclopedia)
        encyclopediaButton.setOnClickListener {
            startActivity(Intent(this, Encyclopedia::class.java))
        }
    }

    override fun onPause() {
        super.onPause()

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        for(key in arrayOf("heartsRemaining", "instrList", "gameTaskId", "instrR", "instrIJ", "roundNumber")) {
            editor.remove(key)
        }
        if (heartsRemaining.isNotEmpty()) {
            update()
            editor.putString("heartsRemaining", heartsRemaining)
            editor.putString("instrList", instrListToString(instrList))
            editor.putString("gameTaskId", sim.gameTask.info["id"].toString())

            editor.putString("instrR", stringArrayToRegList(keyboardData[0]))
            editor.putString("instrIJ", stringArrayToRegList(keyboardData[1]))

            editor.putInt("roundNumber", round)
        }
        editor.apply()
    }
}