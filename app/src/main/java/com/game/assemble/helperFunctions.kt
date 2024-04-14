package com.game.assemble

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

fun getFormatFromOperator(operator: String): Array<Int> {
    if (operator == "ADD") {
        return arrayOf(R.id.operatorKeyboardLayout,
            R.id.digitsKeyboardLayout,
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
            return holder.itemView.findViewById(R.id.gameInstructionTextView7)
        }
        else {
            return button
        }
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

fun removeBlinking(button: TextView) {
    button.setTextColor(Color.BLACK)
}

fun addBlinking(button:TextView) {
    button.setTextColor(Color.GREEN)
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
                    removeBlinking(GameActivity.lastAccessedGameButton!!)
                }
                GameActivity.lastAccessedGameButton = buttons[i]
                addBlinking(buttons[i])
            }
            buttons[i].visibility = View.VISIBLE
        }
        else {
            buttons[i].visibility = View.VISIBLE
            buttons[i].text = template[i]
        }
    }
}