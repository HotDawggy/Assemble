package com.game.assemble

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
            val targetButton: TextView? = GameActivity.lastAccessedGameButton
            if (targetButton == null) {
                // do nothing
            }
            else {
                // -> if target button is "number" -> append to content
                if (GameActivity.getVisibleKeyboardLayout() == R.id.digitsKeyboardLayout) {
                    // append digit to view
                    targetButton.text = targetButton.text.toString() + button.text.toString()
                }
                else { // -> if target button is not "number" -> replace content
                    targetButton.text = button.text.toString()
                }
            }
        }

        return button
    }
}