package com.game.assemble

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

fun getFormatFromOperator(operator: String): Array<Int> {
    if (operator == "ADD") {
        return arrayOf(R.id.operatorKeyboardLayout,
            R.id.shamtDigitKeyboardLayout,
            R.id.registersKeyboardLayout,
            R.id.lineNumberKeyboardLayout)
    }
    else {
        return arrayOf(R.id.operatorKeyboardLayout,
            R.id.registersKeyboardLayout)
    }
}

fun setButtonOnClickKeyboard(button: TextView, keyboardLayout: Int) {
    // TODO: THIS
}

fun Update(buttons: Array<TextView>): Instruction {
    return Instruction() // actually, just loop through all lines of code and update
}

fun getPrevButton(button: TextView): TextView {
    val parent = button.parent as ViewGroup
    val siblingButtons = getSiblingTextViewButtonList(button)
    val index = siblingButtons.indexOf(button)
    if (index == 0) {
        val recyclerView = parent.parent as RecyclerView
        val position = recyclerView.getChildAdapterPosition(parent)
        if (position > 0) {
            val holder =
                recyclerView.findViewHolderForAdapterPosition(position - 1) as RecyclerView.ViewHolder
            val ids = arrayOf(
                holder.itemView.findViewById<TextView>(R.id.gameInstructionTextView7),
                holder.itemView.findViewById(R.id.gameInstructionTextView5),
                holder.itemView.findViewById(R.id.gameInstructionTextView3),
                holder.itemView.findViewById(R.id.gameInstructionTextView1)
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
    val res = arrayOf(
        parent.findViewById<TextView>(R.id.gameInstructionTextView1),
        parent.findViewById<TextView>(R.id.gameInstructionTextView2),
        parent.findViewById<TextView>(R.id.gameInstructionTextView3),
        parent.findViewById<TextView>(R.id.gameInstructionTextView4),
        parent.findViewById<TextView>(R.id.gameInstructionTextView5),
        parent.findViewById<TextView>(R.id.gameInstructionTextView6),
        parent.findViewById<TextView>(R.id.gameInstructionTextView7),
        parent.findViewById<TextView>(R.id.gameInstructionTextView8)
    )
    return res
}

fun getSiblingTextViewButtonList(button: TextView): Array<TextView> {
    val parent = button.parent as ViewGroup
    val res = arrayOf(
        parent.findViewById<TextView>(R.id.gameInstructionTextView1),
        parent.findViewById<TextView>(R.id.gameInstructionTextView3),
        parent.findViewById<TextView>(R.id.gameInstructionTextView5),
        parent.findViewById<TextView>(R.id.gameInstructionTextView7)
    )
    return res

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
