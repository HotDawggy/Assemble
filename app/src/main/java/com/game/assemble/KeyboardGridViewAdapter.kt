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
                    if (targetButton.text == "_") targetButton.text = ""
                    targetButton.text = targetButton.text.toString() + button.text.toString()
                }
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.operatorKeyboardLayout) {
                    val template = Instruction(arrayOf(button.text.toString())).getTemplateFromOperator()
                    val buttonList = getSiblingButtonList(targetButton)
                    Log.i("button list", buttonList.size.toString())

                    for (i in 0 until 8) {
                        Log.i("i is ", i.toString())
                        val currentButton = buttonList[i]
                        if (i >= template.size) {
                            currentButton.visibility = View.INVISIBLE
                            currentButton.text = "_"
                        }
                        else if (template[i] != "_") {
                            currentButton.visibility = View.VISIBLE
                            currentButton.text = template[i]
                        }
                        else {
                            currentButton.visibility = View.VISIBLE
                            currentButton.text = if (currentButton.text == "") {
                                "_"
                            }
                            else {
                                currentButton.text
                            }
                        }
                    }
                    targetButton.text = button.text
                }
                else { // -> if target button is not "number" -> replace content
                    targetButton.text = button.text.toString()
                }
            }
        }

        return button
    }
}