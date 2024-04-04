package com.game.assemble

// from https://developer.android.com/develop/ui/views/layout/recyclerview
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardRecyclerViewAdapter(private val dataSet: Array<LeaderboardItem>) :
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
        viewHolder.rankingView.text = dataSet[position].ranking;
        viewHolder.scoreView.text = dataSet[position].score.toString();
        viewHolder.usernameView.text = dataSet[position].username;
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
