package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EncyclopediaRecyclerViewAdapter(private val dataSet: Array<EncyclopediaItem>) :
    RecyclerView.Adapter<EncyclopediaRecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView
        val descriptionView: TextView

        init {
            titleView = view.findViewById(R.id.encyclopediaItemTextViewTitle)
            descriptionView = view.findViewById(R.id.encyclopediaItemTextViewDescription)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.encyclopedia_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element

        viewHolder.titleView.text = dataSet[position].name
        viewHolder.descriptionView.text = dataSet[position].description
        viewHolder.descriptionView.visibility = View.GONE

        viewHolder.itemView.setOnClickListener {
            if (viewHolder.descriptionView.visibility == View.GONE) {
                viewHolder.descriptionView.visibility = View.VISIBLE
                viewHolder.titleView.text = dataSet[position].usage
            }
            else {
                viewHolder.descriptionView.visibility = View.GONE
                viewHolder.titleView.text = dataSet[position].name
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
