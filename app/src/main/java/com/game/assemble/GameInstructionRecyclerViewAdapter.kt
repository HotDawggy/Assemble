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

        // Set the line number and make it visible
        viewHolder.txVArr[0].text = (position + 1).toString()
        viewHolder.txVArr[0].visibility = View.VISIBLE

        /*
        TODO: Set operator onClickListener to update textViews based on templates

        TODO: If operator is blank, disable and grey out all other textViews
        TODO: If operator is NOT blank, set keyboard layout and enable visibility of other textviews (simulate click is sufficient, probably)
         */
        val opButton = viewHolder.txVArr[1]
        val paramButtons = arrayOf(
            viewHolder.txVArr[3],
            viewHolder.txVArr[5],
            viewHolder.txVArr[7]
        )

        for (i in 0 until 4) {
            val button = if(i == 0) {
                opButton
            } else {
                paramButtons[i - 1]
            }

            button.text = if (i < instr.instr.size) {
                button.visibility = View.VISIBLE
                instr.instr[i]
            } else {
                button.visibility = View.INVISIBLE
                "_"
            }
            if (button.text == "") {
                button.text = "_"
            }

            button.setOnClickListener {
                if (GameActivity.lastAccessedGameButton == button) {
                    removeBlinking(button)
                    GameActivity.lastAccessedGameButton = null
                    GameActivity.switchKeyboardLayout(R.id.registersKeyboardLayout)
                }
                else {
                    if (GameActivity.lastAccessedGameButton != null) {
                        removeBlinking(GameActivity.lastAccessedGameButton!!)
                    }
                    GameActivity.lastAccessedGameButton = button
                    addBlinking(button)
                    GameActivity.switchKeyboardLayout(
                        getKeyboardFromOperator(opButton.text.toString())[i]
                    )
                }
            }
        }

        if (opButton.text == "" || opButton.text == "_") {
            for (button in paramButtons) {
                button.text = "_"
                button.visibility = View.INVISIBLE
            }
        }
        else {
            opButton.callOnClick()
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = instrArr.size

    @RequiresApi(Build.VERSION_CODES.O)
    fun removeBlinking(button: TextView) {
        button!!.setTypeface(
            activity.resources.getFont(
                R.font.consolas
            )
        )
        timeout.removeCallbacks(r);
        this.visible = false
        button.setTextColor(button.textColors.withAlpha(135))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addBlinking(button:TextView) {
        r = Runnable {  // Create a runnable interface that loops between setting the opacity of the text
            visible = if (visible) {
                button.setTextColor(button.textColors.withAlpha(0))
                false
            } else {
                button.setTextColor(button.textColors.withAlpha(135))
                true
            }
            timeout.postDelayed(r, 500);  // Loop the runnable interface with a 500ms delay
        }
        timeout.postDelayed(r, 500);    // Start the runnable interface with a 500ms delay
        button.setTypeface(activity.resources.getFont(R.font.consolas_bold))  // Also bolds the font
    }
}
