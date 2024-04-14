package com.game.assemble

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

// TODO: IMPLEMENT THIS
fun getParamNumber(operator: String): Int {
    if (operator == "ADD") {
        return 2
    }
    else {
        return 3
    }
}

// TODO: THIS
fun getFormatFromOperator(operator: String): Array<Int> {
    if (operator == "ADD") {
        return arrayOf(R.id.operatorKeyboardLayout,
            R.id.digitsKeyboardLayout,
            R.id.gameInstructionRegisterLayout2,
            R.id.lineNumberKeyboardLayout)
    }
    else {
        return arrayOf(R.id.operatorKeyboardLayout,
            R.id.gameInstructionRegisterLayout2)
    }
}

fun setButtonOnClickKeyboard(button: TextView, keyboardLayout: Int) {
    // TODO: THIS
}

fun Update(buttons: Array<TextView>): Instruction {
    return Instruction()
}

// TODO:
// In a Kotlin file
fun changeLayoutVisibility(context: Context, layoutId: Int, visibility: Int) {
    val layout = (context as AppCompatActivity).findViewById<View>(layoutId)
    layout?.visibility = visibility
}

// TODO:
fun getPrevButton(button: TextView): TextView {
    val parent = button.parent as ViewGroup
    val siblingButtons = arrayOf(
        parent.findViewById<TextView>(R.id.gameInstructionTextView1),
        parent.findViewById<TextView>(R.id.gameInstructionTextView3),
        parent.findViewById<TextView>(R.id.gameInstructionTextView5),
        parent.findViewById<TextView>(R.id.gameInstructionTextView7)
    )
    val index = siblingButtons.indexOf(button)
    if (index == 0) {
        val recyclerView = parent.parent as RecyclerView
        val position = recyclerView.getChildAdapterPosition(parent)
        if (position > 0) {
            val holder =
                recyclerView.findViewHolderForAdapterPosition(position - 1) as RecyclerView.ViewHolder
            return holder.itemView.findViewById(R.id.gameInstructionTextView7)
        }
        else return button
    }
    else {
        return siblingButtons[index - 1]
    }
}
