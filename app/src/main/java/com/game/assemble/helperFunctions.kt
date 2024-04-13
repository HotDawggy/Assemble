package com.game.assemble

import android.content.Context
import android.view.View
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

// TODO: IMPLEMENT THIS
fun getKeyboardLayout(operator: String, position: Int): Int {
    if (position == 1) {
        return R.id.operatorKeyboardLayout
    }
    else if (position == 2) {
        return R.id.digitsKeyboardLayout
    }
    else {
        return R.id.lineNumberKeyboardLayout
    }
}

// TODO:
// In a Kotlin file
fun changeLayoutVisibility(context: Context, layoutId: Int, visibility: Int) {
    val layout = (context as AppCompatActivity).findViewById<View>(layoutId)
    layout?.visibility = visibility
}
