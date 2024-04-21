package com.game.assemble

import android.content.Context
import android.os.Looper
import android.os.Handler
import android.util.AttributeSet

class Typewriter(ctx: Context, attrSet: AttributeSet): androidx.appcompat.widget.AppCompatTextView(ctx, attrSet) {
    private var text: CharSequence = ""
    private var idx: Int = 0
    private var delay: Long = 20;
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var adder : Runnable
    init {
        adder = Runnable {
            setText(text.subSequence(0, idx++))
            if (idx < text.length) {
                handler.postDelayed(adder, delay)
            }
        }
    }
    fun animateText(input: CharSequence) {
        text = input
        idx = 0
        setText("")
        handler.removeCallbacks(adder)
        handler.postDelayed(adder, delay)
    }

    fun setDelay(input: Long) {
        delay = input
    }
}