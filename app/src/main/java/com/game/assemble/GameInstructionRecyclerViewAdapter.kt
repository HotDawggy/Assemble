package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameInstructionRecyclerViewAdapter(private val dataSet: Array<InstructionItem>) :
    RecyclerView.Adapter<GameInstructionRecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: LinearLayout
        val lineNumber: TextView
        val operator: Button
        val reg1: Button
        val reg2: Button
        val reg3: Button

        init {
            layout = view.findViewById(R.id.gameInstructionItemLayout)
            lineNumber = view.findViewById(R.id.gameInstructionItemLineNumberTextView)
            operator = view.findViewById(R.id.gameInstructionOpButton)
            reg1 = view.findViewById(R.id.gameInstructionParam1Button)
            reg2 = view.findViewById(R.id.gameInstructionParam2Button)
            reg3 = view.findViewById(R.id.gameInstructionParam3Button)
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
        viewHolder.lineNumber.text = (position + 1).toString()
        viewHolder.operator.text = dataSet[position].op
        viewHolder.reg1.text = dataSet[position].reg1
        viewHolder.reg2.text = dataSet[position].reg2
        viewHolder.reg3.text = dataSet[position].reg3

        val opButton = viewHolder.operator
        val regButtons = arrayOf(
            viewHolder.reg1,
            viewHolder.reg2,
            viewHolder.reg3
        )

        val keyboardLayouts = GameActivity.keyboardLayouts


        // set lastAccessedGameButton
        for(button: Button in arrayOf(viewHolder.operator, viewHolder.reg1, viewHolder.reg2, viewHolder.reg3)) {
            if (button == viewHolder.operator) {
                button.setOnClickListener {
                    val otherButton = GameActivity.lastAccessedGameButton
                    if (otherButton != null) {
                        otherButton.setBackgroundColor(Color.BLUE)
                    }
                    GameActivity.lastAccessedGameButton = button
                    button.setBackgroundColor(Color.GREEN)
                }

            }
            else {
                button.setOnClickListener {
                    val otherButton = GameActivity.lastAccessedGameButton
                    if (otherButton != null) {
                        otherButton.setBackgroundColor(Color.BLUE)
                    }
                    GameActivity.lastAccessedGameButton = button
                    button.setBackgroundColor(Color.GREEN)
                }
            }
        }



        // used when loading from saved game
        if (opButton.text == "") {// force users to input operator first
            // grey out the remaining buttons
            for (button in regButtons) {
                button.visibility = View.INVISIBLE
            }

            // disable keyboard when these buttons are selected
            for (button in regButtons) {
                button.setOnClickListener {
                    // disable all keyboards
                    for (keyboard in keyboardLayouts) {
                        keyboard.visibility = View.GONE
                    }

                    // re-enable register view
                    keyboardLayouts[3].visibility = View.VISIBLE
                }
            }
        }
        else {
            // find number of buttons required for params
            val paramNumber = getParamNumber(opButton.text.toString())

            for(i in 0 until paramNumber) {
                val button = regButtons[i] // get the i-th button
                button.visibility = View.VISIBLE // un-grey out

                val keyboard: LinearLayout = viewHolder.itemView.findViewById(getKeyboardLayout(opButton.text.toString(), i + 1))
                button.setOnClickListener {
                    // disable all keyboards
                    for (keyboard in keyboardLayouts) {
                        keyboard.visibility = View.GONE
                    }
                    keyboard.visibility =View.VISIBLE
                }
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
