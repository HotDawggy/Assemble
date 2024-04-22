package com.game.assemble

import android.content.Context
import android.os.Looper
import android.os.Handler
import android.util.AttributeSet

class Typewriter(ctx: Context, attrSet: AttributeSet): androidx.appcompat.widget.AppCompatTextView(ctx, attrSet) {
    private var text: CharSequence = ""
    private var idx: Int = 0
    private var delay: Long = 0
    private lateinit var adder: Runnable
    private var isBlinkable = false
    private var isBlinking = false
    private lateinit var blink: Runnable
    private var handler = Handler(Looper.getMainLooper())


    init {
        adder = Runnable {
            setText(text.subSequence(0, ++idx))
            if (idx < text.length) {
                handler.postDelayed(adder, delay)
            } else if (isBlinkable && !isBlinking) {
                text = buildString {
                    append(text.toString())
                    append('_') }
                setText(text)
                handler.postDelayed(blink, 300)
            }
        }
        blink = Runnable {
            isBlinking = true
            if (text[text.lastIndex] == '_') {
                text = text.subSequence(0, text.lastIndex) as String + ' '
            } else {
                text = text.subSequence(0, text.lastIndex) as String + '_'
            }
            setText(text)
            handler.postDelayed(blink, 300)
        }
    }

    fun clearText() {
        text = ""
        idx = 0
        handler.removeCallbacks(adder)
    }
    fun animateText(input: CharSequence, blinkable: Boolean = false) {
        isBlinkable = blinkable
        text = input
        idx = 0
        setText("")
        handler.removeCallbacks(adder)
        handler.postDelayed(adder, delay)
    }

    fun appendText(input: CharSequence, blinkable: Boolean = false) {
        isBlinkable = blinkable
        if (isBlinking) {
            isBlinking = false
            text.subSequence(0, text.lastIndex)
            handler.removeCallbacks(blink)
        }
        text = (text.toString() + input)
        handler.removeCallbacks(adder)
        handler.postDelayed(adder, delay)
    }

    fun setDelay(input: Long) {
        delay = input
    }
}