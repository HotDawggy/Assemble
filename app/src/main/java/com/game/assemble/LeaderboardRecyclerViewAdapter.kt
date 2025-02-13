package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardRecyclerViewAdapter(private var dataSet: Array<LeaderboardItem>) :
    RecyclerView.Adapter<LeaderboardRecyclerViewAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankingView: TextView
        val usernameView: TextView
        val scoreView: TextView
        init {
            // Define click listener for the ViewHolder's View
            rankingView = view.findViewById(R.id.leaderboardRowRanking)
            usernameView = view.findViewById(R.id.leaderboardRowUsername)
            scoreView = view.findViewById(R.id.leaderboardRowScore)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.leaderboard_row, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.rankingView.text = dataSet[position].ranking
        viewHolder.scoreView.text = if (dataSet[position].score != null) dataSet[position].score.toString()
                                    else ""
        viewHolder.usernameView.text = dataSet[position].username

        viewHolder.rankingView.setTextColor(Color.WHITE)
        viewHolder.scoreView.setTextColor(Color.WHITE)
        viewHolder.usernameView.setTextColor(Color.WHITE)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

    fun updateData(newData: Array<LeaderboardItem>) {
        this.dataSet = newData
        notifyDataSetChanged()
    }
}
