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
                else if (GameActivity.getVisibleKeyboardLayout() == R.id.operatorKeyboardLayout) {
                    // WIPE OUT ALL BUTTON TEXTS
                    val instructionParent = targetButton.parent as ViewGroup
                    val instructionButtons = arrayOf<TextView>(
                        instructionParent.findViewById(R.id.gameInstructionTextView1),
                        instructionParent.findViewById(R.id.gameInstructionTextView3),
                        instructionParent.findViewById(R.id.gameInstructionTextView5),
                        instructionParent.findViewById(R.id.gameInstructionTextView7)
                    )
                    for(button in instructionButtons) {
                        button.text = "_"
                    }

                    // CHANGE THE FORMAT OF BUTTONS
                    val newFormat: Array<Int> = getFormatFromOperator(button.text.toString())
                    for(i in 0 until newFormat.size) {
                        val button = instructionButtons[i]
                        val targetKeyboardLayout = newFormat[i]
                        setButtonOnClickKeyboard(button, targetKeyboardLayout)
                    }

                    // Update the instrList TODO THIS
                    // GameActivity.instrList[position] = Update(instructionButtons)

                    // IF THIS IS THE LAST LINE, UPDATE THE INSTRLIST AND ADD ONE LINE
                    if (position + 1 == GameActivity.instrList.size) {
                        GameActivity.instrList.add(Instruction())
                        // TODO: NOTIFY THE INSTRUTION LIST ADAPTER
                        // this.notifyDataSetChanged()
                    }
                }
                else { // -> if target button is not "number" -> replace content
                    targetButton.text = button.text.toString()
                }
            }
        }

        return button
    }
}