package com.game.assemble

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
