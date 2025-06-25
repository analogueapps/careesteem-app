package com.aits.careesteem.view.auth.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.view.auth.model.CountryList

class CountryListAdapter(
    private val countries: List<CountryList>,
    private val onSelected: (CountryList) -> Unit
) : RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji = itemView.findViewById<TextView>(R.id.tvEmoji)
        val tvName = itemView.findViewById<TextView>(R.id.tvCountry)
        val tvCode = itemView.findViewById<TextView>(R.id.tvCode)

        init {
            itemView.setOnClickListener {
                onSelected(countries[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_country_dropdown, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = countries.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = countries[position]
        holder.tvEmoji.text = item.emoji
        holder.tvName.text = item.country
        holder.tvCode.text = "+${item.country_code}"
    }
}
