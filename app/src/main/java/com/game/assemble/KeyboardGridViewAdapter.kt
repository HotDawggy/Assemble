package com.game.assemble

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.TextView

class KeyboardGridViewAdapter(val context: Context, private val keys: List<String>): BaseAdapter() {
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
            if (targetButton == null || targetButton.text.toString() == "main:") {
                // do nothing
            }
            else {
                // -> if target button is "number" -> append to content
                if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout
                    || GameActivity.getVisibleKeyboardLayout() == R.id.immedDigitKeyboardLayout) {
                    if (button.text.toString() == "-") {
                        targetButton.setTextColor(targetButton.context.getColor(R.color.code_num))
                        if (targetButton.text == "_") targetButton.text = "-"
                    }
                    else {
                        // append digit to view
                        if (targetButton.text == "_") targetButton.text = ""
                        targetButton.setTextColor(targetButton.context.getColor(R.color.code_num))

                        val res = targetButton.text.toString() + button.text.toString()
                        val leftLimit =
                            if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout) 0
                            else -(1 shl 15) + 1
                        val rightLimit =
                            if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout) (1 shl 4) - 1
                            else (1 shl 15) - 1

                        if (res.toInt() in leftLimit..rightLimit) {
                            targetButton.text = res
                        }
                    }

                }
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.operatorKeyboardLayout) {
                    if (Instruction(arrayOf(button.text.toString())).isLabel()) {
                        targetButton.text = button.text.toString() + ":"
                        targetButton.setTextColor(targetButton.context.getColor(R.color.code_label))
                    } else {
                        targetButton.text = "\t" + button.text.toString()
                        targetButton.setTextColor(targetButton.context.getColor(R.color.code_instr))
                    }
                    getSiblingButtonList(targetButton).forEach {
                        if (it != targetButton) it.setTextColor(targetButton.context.getColor(R.color.code))
                    }
                    changeInstructionOppType(targetButton, targetButton.text.toString())
                }
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.labelsKeyboardLayout) { // -> if target button is not "number" -> replace content
                    targetButton.setTextColor(targetButton.context.getColor(R.color.code_label))
                    targetButton.text = button.text.toString()
                } else {
                    targetButton.text = button.text.toString()
                }
            }
        }

        return button
    }
}