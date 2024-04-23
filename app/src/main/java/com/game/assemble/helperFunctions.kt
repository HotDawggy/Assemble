package com.game.assemble

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.allViews

class Helper(val context: Context) {
    fun getNextButton(button: TextView): TextView { // returns null if
        val parent = button.parent as ViewGroup
        assert(parent in GameActivity.instructionLinearLayout.allViews)

        val siblingButtons = getSiblingTextViewButtonList(button)
        val index = siblingButtons.indexOf(button)
        if (index == 3 || siblingButtons[index + 1].visibility != View.VISIBLE) {
            val position = GameActivity.instructionLinearLayout.indexOfChild(parent)
            if (position + 1 == GameActivity.instrList.size) {
                Log.i("ADDING", "NEW LINE")
                val newInstr = Instruction(arrayOf("_"))
                GameActivity.instrList.add(newInstr)
                val view = LayoutInflater.from(context).inflate(R.layout.game_instruction_item, null)
                GameActivity.modifyView(view, position + 1, newInstr)
                GameActivity.instructionLinearLayout.addView(view)
            }
            return GameActivity.instructionLinearLayout.getChildAt(position + 1).findViewById(R.id.gameInstructionTextView1)
        }
        else {
            return siblingButtons[index + 1]
        }
    }
}

fun instrListToString(instrList: MutableList<Instruction>): String {
    var res = mutableListOf<String>()
    for (instruction in instrList) {
        for(i in 0 until 4) {
            if (instruction[i] == null || instruction[i] == "") {
                res.add("_")
            }
            else {
                res.add(instruction[i]!!.removePrefix("\t").removeSuffix(":"))
            }
        }
    }
    Log.i("SAVING", res.joinToString("+++"))
    return res.joinToString("+++")
}

fun stringToInstrList(instructions: String): MutableList<Instruction> {
    Log.i("instructions is", instructions)
    var splitString = instructions.split("+++")
    var res = mutableListOf<Instruction>()
    Log.i("instruction size is ", splitString.size.toString())
    for(i in splitString.indices step 4) {
        if (i + 3 >= splitString.size) break
        res.add(Instruction(arrayOf(
            splitString[i], splitString[i + 1], splitString[i + 2], splitString[i + 3]
        )))
    }
    return res.toMutableList()
}

fun regListToStringArray(regList: String): Array<String> {
    return regList.split("+++").toTypedArray()
}

fun stringArrayToRegList(strings: Array<String>): String {
    return strings.joinToString("+++")
}

fun removeOpFromKeyboard(operator: String) {
    if (operator in GameActivity.keyboardData[0]) {
        var tmpData = GameActivity.keyboardData[0].toMutableList()
        tmpData.remove(operator)
        GameActivity.keyboardData[0] = tmpData.toTypedArray()
    }
    if (operator in GameActivity.keyboardData[1]) {
        var tmpData = GameActivity.keyboardData[1].toMutableList()
        tmpData.remove(operator)
        GameActivity.keyboardData[1] = tmpData.toTypedArray()
    }
}

fun getPrevButton(button: TextView): TextView {
    val parent = button.parent as ViewGroup
    assert(parent in GameActivity.instructionLinearLayout.allViews)

    val siblingButtons = getSiblingTextViewButtonList(button)
    val index = siblingButtons.indexOf(button)
    if (index == 0) {
        val position = GameActivity.instructionLinearLayout.indexOfChild(parent)
        if (position > 0) {
            val holder =
                GameActivity.instructionLinearLayout.getChildAt(position - 1)
            val ids = arrayOf(
                holder.findViewById<TextView>(R.id.gameInstructionTextView7),
                holder.findViewById<TextView>(R.id.gameInstructionTextView5),
                holder.findViewById<TextView>(R.id.gameInstructionTextView3),
                holder.findViewById<TextView>(R.id.gameInstructionTextView1),
            )
            for (button in ids) {
                if (button.visibility == View.VISIBLE) {
                    return button
                }
            }
        }
        return button
    }
    else {
        return siblingButtons[index - 1]
    }
}

fun getSiblingButtonList(button: TextView): Array<TextView> {
    val parent = button.parent as ViewGroup
    return arrayOf(
        parent.findViewById(R.id.gameInstructionTextView1),
        parent.findViewById(R.id.gameInstructionTextView2),
        parent.findViewById(R.id.gameInstructionTextView3),
        parent.findViewById(R.id.gameInstructionTextView4),
        parent.findViewById(R.id.gameInstructionTextView5),
        parent.findViewById(R.id.gameInstructionTextView6),
        parent.findViewById(R.id.gameInstructionTextView7),
        parent.findViewById(R.id.gameInstructionTextView8)
    )
}

fun getSiblingTextViewButtonList(button: TextView): Array<TextView> {
    val parent = button.parent as ViewGroup
    return arrayOf(
        parent.findViewById(R.id.gameInstructionTextView1),
        parent.findViewById(R.id.gameInstructionTextView3),
        parent.findViewById(R.id.gameInstructionTextView5),
        parent.findViewById(R.id.gameInstructionTextView7)
    )

}

fun getKeyboardFromOperator(op: String?): Array<Int> {
    return arrayOf(R.id.operatorKeyboardLayout) + Instruction(arrayOf(op)).getKeyboardFromOperator().toTypedArray()
}

fun changeInstructionOppType(button: TextView, opType: String) {
    val buttons = getSiblingButtonList(button) // 1, 2, ... 8
    val template = Instruction(arrayOf(opType)).getTemplateFromOperator()
    val keyboards = getKeyboardFromOperator(opType)

    // TODO: replace on click listeners
    // TODO: templates
    for(i in 0 until 8) {
        if (i >= template.size) {
            buttons[i].visibility = View.INVISIBLE
            buttons[i].text = "_"
        }
        else if (template[i] == "_") {
            buttons[i].setOnClickListener {
                // TODO: bring up correct keyboard type
                GameActivity.switchKeyboardLayout(keyboards[i / 2])

                if (GameActivity.lastAccessedGameButton != null) {
                    GameActivity.removeSelected()
                }
                GameActivity.lastAccessedGameButton = buttons[i]
                GameActivity.addSelected(buttons[i])
            }
            if (i > 1) buttons[i].text = "_"    // Clears the fields
            buttons[i].visibility = View.VISIBLE
        }
        else {
            buttons[i].visibility = View.VISIBLE
            buttons[i].text = template[i]
        }
    }
}
