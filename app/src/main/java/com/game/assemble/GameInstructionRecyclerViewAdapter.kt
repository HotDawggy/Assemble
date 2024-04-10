package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
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

        val visibleRegs = getParamNumber(dataSet[position].op)
        val visibilities = List(visibleRegs) {View.VISIBLE} + List(3 - visibleRegs) {View.INVISIBLE}

        viewHolder.reg1.visibility = visibilities[0]
        viewHolder.reg2.visibility = visibilities[1]
        viewHolder.reg3.visibility = visibilities[2]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
