package com.aits.careesteem.view.alerts.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemBodyMapAddedBinding
import com.aits.careesteem.utils.AlertUtils
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

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = position

    inner class ViewHolder(private val binding: ItemBodyMapAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: FileModel) {
            try {
                binding.apply {
                    tvBodyPartNames.text = data.bodyPartNames

                    // Load local image using Glide
                    Glide.with(context)
                        .load(File(data.filePath))
                        .override(400, 300)
                        
                        .into(fileImageView)

                    // Handle delete button click
                    btnDelete.setOnClickListener {
                        onDelete(adapterPosition)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("FileAdapter", "" + e.printStackTrace())
            }
        }
    }
}
