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

// TODO:
// In a Kotlin file
fun changeLayoutVisibility(context: Context, layoutId: Int, visibility: Int) {
    val layout = (context as AppCompatActivity).findViewById<View>(layoutId)
    layout?.visibility = visibility
}
