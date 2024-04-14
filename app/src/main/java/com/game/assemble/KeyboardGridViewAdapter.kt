package com.game.assemble

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView

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
            val targetButton: TextView? = GameActivity.lastAccessedGameButton
            if (targetButton == null) {
                // do nothing
            }
            else {
                // -> if target button is "number" -> append to content
                if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout) {
                    // append digit to view
                    if (targetButton.text == "_") targetButton.text = ""
                    targetButton.text = targetButton.text.toString() + button.text.toString()
                }
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.operatorKeyboardLayout) {
                    targetButton.text = button.text.toString()
                    changeInstructionOppType(targetButton, targetButton.text.toString())
                }
                else { // -> if target button is not "number" -> replace content
                    targetButton.text = button.text.toString()
                }
            }
        }

        return button
    }
}