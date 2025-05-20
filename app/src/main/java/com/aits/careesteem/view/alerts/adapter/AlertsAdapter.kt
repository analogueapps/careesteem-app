package com.aits.careesteem.view.alerts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.BuildConfig
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemAlertListBinding
import com.aits.careesteem.databinding.ItemBodyMapAddedBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.alerts.model.AlertListResponse
import com.bumptech.glide.Glide

class AlertsAdapter(
    private val context: Context
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    private var alertList = listOf<AlertListResponse.Data>()

    fun updateList(newList: List<AlertListResponse.Data>) {
        alertList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alertList[position])
    }

    override fun getItemCount(): Int = alertList.size

    inner class AlertViewHolder(private val binding: ItemAlertListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: AlertListResponse.Data) = with(binding) {
            val formattedDate = AppConstant.alertsListTimer(data.created_at)
            alertName.text = "${data.client_name}\t\t$formattedDate"

//            // Toggle expand/collapse on click
//            alertLayout.setOnClickListener {
//                val isVisible = alertName.tag == "Visible"
//                alertName.tag = if (isVisible) "Invisible" else "Visible"
//                val icon = if (isVisible) R.drawable.ic_keyboard_arrow_down else R.drawable.ic_keyboard_arrow_up
//                alertName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(icon), null)
//                detailLayout.visibility = if (isVisible) View.GONE else View.VISIBLE
//            }

            val isExpanded = position == expandedPosition
            detailLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            alertName.tag = if (isExpanded) "Visible" else "Invisible"
            val icon = if (isExpanded) R.drawable.ic_keyboard_arrow_up else R.drawable.ic_keyboard_arrow_down
            alertName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(icon), null)

            // Toggle expand/collapse on click
            alertLayout.setOnClickListener {
                val previousExpandedPosition = expandedPosition
                expandedPosition = if (isExpanded) {
                    RecyclerView.NO_POSITION // collapse
                } else {
                    position // expand this one
                }
                notifyItemChanged(previousExpandedPosition)
                notifyItemChanged(position)
            }

            // Populate alert details
            clientName.text = data.client_name
            visitTime.text = data.session_time
            severityOfConcern.text = data.severity_of_concern
            visitNotes.text = data.concern_details

            // Map body parts with corresponding images
            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_image.getOrNull(index).orEmpty()
                )
            }

            // Setup nested image recycler
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = ServerImageAdapter(context, bodyMapItems)
        }
    }
}

class ServerImageAdapter(
    private val context: Context,
    private val imageList: List<BodyMapItem>
) : RecyclerView.Adapter<ServerImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemBodyMapAddedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(imageList[position])
    }

    override fun getItemCount(): Int = imageList.size

    inner class ImageViewHolder(private val binding: ItemBodyMapAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: BodyMapItem) = with(binding) {
            tvBodyPartNames.text = data.partName

            Glide.with(context)
                .load("${BuildConfig.API_BASE_URL}${data.imageUrl.replace("\\", "/")}")
                .placeholder(R.drawable.logo_preview)
                .into(fileImageView)

            btnDelete.visibility = View.GONE
        }
    }
}

data class BodyMapItem(
    val partName: String,
    val imageUrl: String
)
