package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView


class GameInstructionRecyclerViewAdapter(private val instrArr: Array<Instruction>, private val activity: GameActivity) :
    RecyclerView.Adapter<GameInstructionRecyclerViewAdapter.ViewHolder>() {
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    private var timeout: Handler = Handler(Looper.getMainLooper())
    private var r: Runnable = Runnable{}
    private var visible = true
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
    @RequiresApi(Build.VERSION_CODES.O)
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
            val keyboardLayouts = GameActivity.keyboardLayouts
            viewHolder.txVArr[i].text = instrStr[i - 1]
            viewHolder.txVArr[i].visibility = View.GONE
            if (!(instrStr[i - 1].contains("\t") || instrStr[i - 1].contains("(") || instrStr[i - 1].contains(")"))) {
                viewHolder.txVArr[i].setOnClickListener {
                    if (GameActivity.lastAccessedGameButton != null) {
                        GameActivity.lastAccessedGameButton!!.setTypeface(
                            activity.resources.getFont(
                                R.font.consolas
                            )
                        )
                        timeout.removeCallbacks(r);
                        visible = true
                        GameActivity.lastAccessedGameButton?.setTextColor(GameActivity.lastAccessedGameButton!!.textColors.withAlpha(135))
                    }
                    if (GameActivity.lastAccessedGameButton == null || GameActivity.lastAccessedGameButton != viewHolder.txVArr[i]) {
                        GameActivity.lastAccessedGameButton = viewHolder.txVArr[i]
                        r = Runnable {
                            visible = if (visible) {
                                viewHolder.txVArr[i].setTextColor(viewHolder.txVArr[i].textColors.withAlpha(0))
                                false
                            } else {
                                viewHolder.txVArr[i].setTextColor(viewHolder.txVArr[i].textColors.withAlpha(135))
                                true
                            }
                            timeout.postDelayed(r, 500);
                        }
                        timeout.postDelayed(r, 500);
                        viewHolder.txVArr[i].setTypeface(activity.resources.getFont(R.font.consolas_bold))
                        for (keyboard in keyboardLayouts) {
                            keyboard.visibility = View.GONE
                            if (keyboard.id == Instruction.getKeyboardLayout(
                                    Instruction.getField(instr)[buttonArr.indexOf(
                                        viewHolder.txVArr[i].id
                                    )]
                                )
                            )
                                keyboard.visibility = View.VISIBLE
                        }
                    } else {
                        GameActivity.lastAccessedGameButton = null
                        for (keyboard in keyboardLayouts) {
                            keyboard.visibility = View.GONE
                            if (keyboard.id == R.id.gameInstructionRegisterLayout2)
                                keyboard.visibility = View.VISIBLE
                        }
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
