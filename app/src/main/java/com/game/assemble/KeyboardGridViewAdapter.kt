package com.game.assemble

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged

class KeyboardGridViewAdapter(val context: Context, val keys: List<String>): BaseAdapter() {
    override fun getCount(): Int {
        return keys.size
    }

    override fun getItem(position: Int): String {
        return keys[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val key = getItem(position)
        val button = Button(context)
        button.text = key
        button.setOnClickListener {
            val toFillButton: Button? = GameActivity.lastAccessedGameButton
            if (toFillButton != null) {
                toFillButton.text = button.text.toString()

                val parentViewGroup = toFillButton.parent as? ViewGroup
                val regButton = parentViewGroup!!.findViewById<Button>(R.id.gameInstructionOpButton)
                if (regButton == toFillButton) { // the user has just added/changed an operator
                    // find number of buttons required for params
                    val paramNumber = getParamNumber(toFillButton.text.toString())
                    val regButtons = arrayOf<Button>(
                        parentViewGroup!!.findViewById(R.id.gameInstructionParam1Button),
                        parentViewGroup!!.findViewById(R.id.gameInstructionParam2Button),
                        parentViewGroup!!.findViewById(R.id.gameInstructionParam3Button),
                    )

                    for(i in 0 until 3) {
                        if (i < paramNumber) {
                            val button = regButtons[i] // get the i-th button
                            button.visibility = View.VISIBLE // un-grey out

                            button.setOnClickListener {
                                // disable all keyboards
                                for(keyboard in GameActivity.keyboardLayouts) {
                                    keyboard.visibility = View.GONE
                                }

                                changeLayoutVisibility(context, getKeyboardLayout(regButton.text.toString(), i + 1), View.VISIBLE)

                                val otherButton = GameActivity.lastAccessedGameButton
                                if (otherButton != null) {
                                    otherButton.setBackgroundColor(Color.BLUE)
                                }
                                GameActivity.lastAccessedGameButton = button
                                button.setBackgroundColor(Color.GREEN)

                            }
                        }
                        else {
                            val button = regButtons[i]
                            button.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
        return button
    }
}