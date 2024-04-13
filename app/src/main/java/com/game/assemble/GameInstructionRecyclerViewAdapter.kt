package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameInstructionRecyclerViewAdapter(private val instrArr: Array<Instruction>, private val activity: GameActivity) :
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
        if (Instruction.isInit()) {
            Instruction.initLookup(activity)
        }
        val instrStr = Instruction.stringify(instr, activity)
        viewHolder.txVArr[0].text = (position + 1).toString()
        viewHolder.txVArr[0].visibility = View.VISIBLE
        var buttonArr = intArrayOf()

        for (i in 1..instrStr.size) {
            viewHolder.txVArr[i].text = instrStr[i - 1]
            viewHolder.txVArr[i].visibility = View.GONE
            if (!(instrStr[i - 1].contains("\t") || instrStr[i - 1].contains("(") || instrStr[i - 1].contains(")"))) {
                viewHolder.txVArr[i].setOnClickListener {
                    val cur = i
                    GameActivity.lastAccessedGameButton?.typeface = Typeface.DEFAULT
                    GameActivity.lastAccessedGameButton = viewHolder.txVArr[i]
                    viewHolder.txVArr[i].typeface = Typeface.DEFAULT_BOLD
                    val keyboardLayouts = GameActivity.keyboardLayouts
                    for (keyboard in keyboardLayouts) {
                        keyboard.visibility = View.GONE
                        if (keyboard.id == Instruction.getKeyboardLayout(Instruction.getField(instr)[buttonArr.indexOf(viewHolder.txVArr[cur].id)]))
                            keyboard.visibility = View.VISIBLE
                    }
                }
                buttonArr += viewHolder.txVArr[i].id
            }
        }
        viewHolder.txVArr[1].visibility = View.VISIBLE
        if (viewHolder.txVArr[1].text != "_") {
            for (i in 2..instrStr.size) {
                viewHolder.txVArr[i].visibility = View.VISIBLE
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = instrArr.size

}
