package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GameRegisterRecyclerViewAdapter(private val dataSet: MutableMap<String, Int>) :
    RecyclerView.Adapter<GameRegisterRecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val registerName: TextView
        val registerValue: TextView

        init {
            registerName = view.findViewById(R.id.gameRegistersItemNameTextView)
            registerValue = view.findViewById(R.id.gameRegistersItemValueTextView)

            registerName.setOnClickListener {
                val targetButton: TextView? = GameActivity.lastAccessedGameButton
                if (targetButton == null) {
                    // do nothing
                }
                else {
                    targetButton.text = registerName.text.toString()
                    if (GameActivity.lastAccessedGameButton != null) {
                        val nextButton = KeyboardGridViewAdapter.myHelper.getNextButton(GameActivity.lastAccessedGameButton!!)
                        GameActivity.removeSelected()
                        nextButton.callOnClick()
                        //GameActivity.addSelected(nextButton)
                    }

                }
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.game_registers_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val pair = dataSet.toList()[position]
        viewHolder.registerName.text = pair.first
        viewHolder.registerValue.text = ("0x" + pair.second.toString(16))
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
