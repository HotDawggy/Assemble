package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class GameInstructionRecyclerViewAdapter(private val instrArr: MutableList<Instruction>) :
    RecyclerView.Adapter<GameInstructionRecyclerViewAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txVArr: Array<TextView>

        init {
            txVArr = arrayOf(
                view.findViewById(R.id.gameInstructionItemLineNumberTextView),
                view.findViewById(R.id.gameInstructionTextView1),
                view.findViewById(R.id.gameInstructionTextView2),
                view.findViewById(R.id.gameInstructionTextView3),
                view.findViewById(R.id.gameInstructionTextView4),
                view.findViewById(R.id.gameInstructionTextView5),
                view.findViewById(R.id.gameInstructionTextView6),
                view.findViewById(R.id.gameInstructionTextView7),
                view.findViewById(R.id.gameInstructionTextView8)

            )
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.game_instruction_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val instr = instrArr[position]

        // Set the line number and make it visible
        viewHolder.txVArr[0].text = (position + 1).toString()
        viewHolder.txVArr[0].visibility = View.VISIBLE

        val opButton = viewHolder.txVArr[1]
        val paramButtons = arrayOf(
            viewHolder.txVArr[3],
            viewHolder.txVArr[5],
            viewHolder.txVArr[7]
        )
        if (instr[0] != "main:") {
            for (i in 0 until 4) {
                var button = opButton
                if (i == 0) {
                    if (instr.isLabel()) {
                        opButton.text = instr[0]
                        opButton.setTextColor(opButton.context.getColor(R.color.code_label))
                    } else {
                        opButton.text = "\t" + instr[0]
                        opButton.setTextColor(opButton.context.getColor(R.color.code_instr))
                    }
                } else {
                    button = paramButtons[i - 1]
                    if (i < getKeyboardFromOperator(instr[0]!!).size) {
                        val color = when(getKeyboardFromOperator(instr[0]!!)[i]) {
                            R.id.immedDigitKeyboardLayout, R.id.shamtDigitKeyboardLayout -> R.color.code_num
                            R.id.labelsKeyboardLayout -> R.color.code_label
                            else -> R.color.code
                        }
                        button.setTextColor(button.context.getColor(color))
                    }
                    if (i < instr.size) {
                        button.visibility = View.VISIBLE
                        button.text = instr[i]
                    } else {
                        button.visibility = View.INVISIBLE
                        button.text = "_"
                    }
                }
                if (button.text == "") {
                    button.text = "_"
                }

                Log.i("text is ", button.text.toString())

                button.setOnClickListener {
                    if (GameActivity.lastAccessedGameButton == button) {
                        GameActivity.removeSelected()
                        GameActivity.switchKeyboardLayout(R.id.registersKeyboardLayout)
                    } else {
                        GameActivity.removeSelected()
                        GameActivity.addSelected(button)
                        GameActivity.switchKeyboardLayout(
                            getKeyboardFromOperator(opButton.text.toString())[i]
                        )
                    }
                }
            }
        } else {
            opButton.text = instr[0]
            opButton.setTextColor(opButton.context.getColor(R.color.code_label))
            opButton.visibility = View.VISIBLE
        }

        if (opButton.text == "" || opButton.text == "_") {
            for (button in paramButtons) {
                button.text = "_"
                button.visibility = View.INVISIBLE
            }
        }
        else {
            val template = Instruction(arrayOf(opButton.text.toString())).getTemplateFromOperator()
            for (i in 0 until 8) {
                val currentButton = viewHolder.txVArr[i + 1]
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
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = instrArr.size

    fun updateItemAtPosition(position: Int, newItem: Instruction) {
        if (position in 0 until instrArr.size) {
            instrArr[position] = newItem
        }
    }
}
