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
import com.aits.careesteem.view.alerts.model.FileModel
import com.aits.careesteem.view.clients.adapter.BehaviourRiskAssessmentHazardsAdapter
import com.bumptech.glide.Glide
import java.io.File

class AlertsAdapter(
    private val context: Context
) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {

    private var adapterList = listOf<AlertListResponse.Data>()

    fun updatedList(list: List<AlertListResponse.Data>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAlertListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemAlertListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: AlertListResponse.Data) {
            binding.apply {
                val dateFormatted = AppConstant.alertsListTimer(data.created_at)
                alertName.text = "${data.client_name}\t\t${dateFormatted}"

                alertLayout.setOnClickListener {
                    if(alertName.tag == "Invisible") {
                        alertName.tag = "Visible"
                        alertName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(
                            R.drawable.ic_keyboard_arrow_up), null)
                        detailLayout.visibility = View.VISIBLE
                    } else {
                        alertName.tag = "Invisible"
                        alertName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getDrawable(
                            R.drawable.ic_keyboard_arrow_down), null)
                        detailLayout.visibility = View.GONE
                    }
                }

                clientName.text = data.client_name
                visitTime.text = data.session_time
                severityOfConcern.text = data.severity_of_concern
                visitNotes.text = data.concern_details

                val bodyMapItems = data.body_part_names.mapIndexed { index, partName ->
                    BodyMapItem(
                        partName = partName,
                        imageUrl = data.body_image.getOrNull(index).orEmpty()
                    )
                }

                val adapter = ServerImageAdapter(context, bodyMapItems)
                binding.recyclerView.layoutManager = LinearLayoutManager(context)
                binding.recyclerView.adapter = adapter
            }
        }
    }
}

class ServerImageAdapter(
    private val context: Context,
    private val adapterList: List<BodyMapItem>
) : RecyclerView.Adapter<ServerImageAdapter.ViewHolder>() {

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

        fun bind(data: BodyMapItem) {
            binding.apply {
                binding.tvBodyPartNames.text = data.partName
                Glide.with(context)
                    .load(BuildConfig.API_BASE_URL+data.imageUrl.replace("\\", "/")) // Assuming the filePath is local
                    .placeholder(R.drawable.logo_preview) // Optional placeholder
                    .into(fileImageView)

                btnDelete.visibility = View.GONE
            }
        }
    }
}

data class BodyMapItem(
    val partName: String,
    val imageUrl: String
)