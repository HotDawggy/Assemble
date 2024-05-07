package com.game.assemble

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.TextView

class KeyboardRemoveOpsGridViewAdapter(val context: Context, private val keys: List<String>): BaseAdapter() {

    override fun getCount(): Int {
        return keys.size
    }

    override fun getItem(position: Int): String {
        return keys[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val button = Button(context)
        button.text = getItem(position)

        button.setOnClickListener {
            (parent as GridView).onItemClickListener?.onItemClick(parent, button, position, getItemId(position))
        }
        return button
    }
}