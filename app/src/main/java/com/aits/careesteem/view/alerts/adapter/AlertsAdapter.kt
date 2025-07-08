package com.aits.careesteem.view.alerts.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemAlertListBinding
import com.aits.careesteem.databinding.ItemBodyMapAddedBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.alerts.model.AlertListResponse
import com.bumptech.glide.Glide

class AlertsAdapter(
    private val context: Context
) : RecyclerView.Adapter<AlertsAdapter.AlertViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION
    private var alertList = listOf<AlertListResponse.Data>()

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newList: List<AlertListResponse.Data>) {
        alertList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(alertList[position], position)
    }

    override fun getItemCount(): Int = alertList.size

    inner class AlertViewHolder(private val binding: ItemAlertListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: AlertListResponse.Data, position: Int) = with(binding) {
            try {
                val formattedDate = AppConstant.alertsListTimer(data.created_at)
                alertName.text = AppConstant.checkClientName(data.client_name)
                alertTime.text = formattedDate

                val isExpanded = position == expandedPosition
                detailLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                alertTime.tag = if (isExpanded) "Visible" else "Invisible"
                val icon = if (isExpanded) R.drawable.round_arrow_up else R.drawable.round_arrow_down
                alertTime.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(icon), null)

                alertLayout.setOnClickListener {
                    val previousExpandedPosition = expandedPosition
                    expandedPosition = if (isExpanded) RecyclerView.NO_POSITION else position
                    notifyItemChanged(previousExpandedPosition)
                    notifyItemChanged(position)
                }

                clientName.text = AppConstant.checkClientName(data.client_name)
                visitTime.text = AppConstant.checkNull(data.session_time)
                severityOfConcern.text = AppConstant.checkNull(data.severity_of_concern)
                visitNotes.text = AppConstant.checkNull(data.concern_details)

                val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                    BodyMapItem(
                        partName = name,
                        imageUrl = data.body_map_image_url.getOrNull(index).orEmpty()
                    )
                }

                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = ServerImageAdapter(context, bodyMapItems)
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("AlertsAdapter", e.toString())
            }
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
            try {
                tvBodyPartNames.text = AppConstant.checkNull(data.partName)

                Glide.with(context)
                    .load(data.imageUrl.replace("\\", "/"))
                    .override(400, 300)
                    
                    .into(fileImageView)

                btnDelete.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("ServerImageAdapter", e.toString())
            }
        }
    }
}

class BodyMapImageAdapter(
    private val context: Context,
    private val imageList: List<BodyMapItem>
) : RecyclerView.Adapter<BodyMapImageAdapter.ImageViewHolder>() {

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
            try {
                tvBodyPartNames.text = AppConstant.checkNull(data.partName)

                Glide.with(context)
                    .load(data.imageUrl.replace("\\", "/"))
                    .override(400, 300)
                    
                    .into(fileImageView)

                btnDelete.visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("BodyMapImageAdapter", e.toString())
            }
        }
    }
}

data class BodyMapItem(
    val partName: String,
    val imageUrl: String
)
