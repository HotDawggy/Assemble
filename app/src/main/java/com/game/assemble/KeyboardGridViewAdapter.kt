package com.game.assemble

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
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
            if (targetButton == null) {
                // do nothing
            }
            else {
                // -> if target button is "number" -> append to content
                if (GameActivity.getVisibleKeyboardLayout() == R.id.shamtDigitKeyboardLayout
                    || GameActivity.getVisibleKeyboardLayout() == R.id.immedDigitKeyboardLayout) {
                    // append digit to view
                    if (targetButton.text == "_") targetButton.text = ""
                    targetButton.setTextColor(targetButton.context.getColor(R.color.code_num))
                    targetButton.text = targetButton.text.toString() + button.text.toString()
                }
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.operatorKeyboardLayout) {
                    targetButton.text = "\t" + button.text.toString()
                    if (Instruction(arrayOf(button.text.toString())).isLabel()) {
                        targetButton.setTextColor(targetButton.context.getColor(R.color.code_label))
                    } else {
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