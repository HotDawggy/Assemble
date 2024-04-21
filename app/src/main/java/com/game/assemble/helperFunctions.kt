package com.game.assemble

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.allViews
import androidx.recyclerview.widget.RecyclerView

fun setButtonOnClickKeyboard(button: TextView, keyboardLayout: Int) {
    // TODO: THIS
}

fun getPrevButton(button: TextView): TextView {
    val parent = button.parent as ViewGroup
    assert(parent in GameActivity.instructionLinearLayout.allViews)

    val siblingButtons = getSiblingTextViewButtonList(button)
    val index = siblingButtons.indexOf(button)
    if (index == 0) {
        val position = GameActivity.instructionLinearLayout.indexOfChild(parent)
        if (position > 1) {
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

fun getNextButton(context: Context, button: TextView): TextView {
    val parent = button.parent as ViewGroup
    assert(parent in GameActivity.instructionLinearLayout.allViews)

    val siblingButtons = getSiblingTextViewButtonList(button)
    val index = siblingButtons.indexOf(button)
    if (index == 3 || siblingButtons[index + 1].visibility != View.VISIBLE) {
        val position = GameActivity.instructionLinearLayout.indexOfChild(parent)
        if (position >= GameActivity.instrList.size) {
            GameActivity.addLine()
        }
        return GameActivity.instructionLinearLayout.getChildAt(position).findViewById(R.id.gameInstructionTextView1)
    }
    else {
        return siblingButtons[index + 1]
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

fun getKeyboardFromOperator(op: String): Array<Int> {
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
