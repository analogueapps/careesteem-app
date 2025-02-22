package com.aits.careesteem.view.alerts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemBodyMapAddedBinding
import com.aits.careesteem.view.alerts.model.FileModel
import com.bumptech.glide.Glide
import java.io.File

class FileAdapter(
    private val context: Context,
    private val adapterList: List<FileModel>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemBodyMapAddedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataItem = adapterList[position]
        holder.bind(dataItem)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemBodyMapAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FileModel) {
            binding.apply {
                tvBodyPartNames.text = data.bodyPartNames
                // Use Glide to load the image from the file path
                Glide.with(context)
                    .load(File(data.filePath)) // Assuming the filePath is local
                    .placeholder(R.drawable.logo_preview) // Optional placeholder
                    .into(fileImageView)

                btnDelete.setOnClickListener {
                    onDelete(position) // Call the delete function
                }
            }
        }
    }
}
