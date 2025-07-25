package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemCareNetworkBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.ClientDetailsResponse

class CareNetworkAdapter(
    private val context: Context,
    private val onMyCareNetworkItemClick: OnMyCareNetworkItemClick
) : RecyclerView.Adapter<CareNetworkAdapter.ViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    interface OnMyCareNetworkItemClick {
        fun onMyCareNetworkItemClicked(data: ClientDetailsResponse.Data.MyCareNetworkData)
    }

    private var adapterList = listOf<ClientDetailsResponse.Data.MyCareNetworkData>()

    fun updateList(list: List<ClientDetailsResponse.Data.MyCareNetworkData>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemCareNetworkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(adapterList[position], position)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ViewHolder(private val binding: ItemCareNetworkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: ClientDetailsResponse.Data.MyCareNetworkData, position: Int) {
            binding.apply {
                occupationType.text = data.occupation_type

                imgCall.setOnClickListener {
                    onMyCareNetworkItemClick.onMyCareNetworkItemClicked(data)
                }

                val isExpanded = position == expandedPosition
                detailLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                alertName.tag = if (isExpanded) "Visible" else "Invisible"
                val icon =
                    if (isExpanded) R.drawable.round_arrow_up else R.drawable.round_arrow_down
                alertName.setImageResource(icon)

                // Toggle expand/collapse on click
                alertName.setOnClickListener {
                    val previousExpandedPosition = expandedPosition
                    expandedPosition = if (isExpanded) {
                        RecyclerView.NO_POSITION // collapse
                    } else {
                        position // expand this one
                    }
                    notifyItemChanged(previousExpandedPosition)
                    notifyItemChanged(position)
                }

                // Toggle expand/collapse on click
                layout.setOnClickListener {
                    val previousExpandedPosition = expandedPosition
                    expandedPosition = if (isExpanded) {
                        RecyclerView.NO_POSITION // collapse
                    } else {
                        position // expand this one
                    }
                    notifyItemChanged(previousExpandedPosition)
                    notifyItemChanged(position)
                }

                // Add data
                binding.occupationType.text = AppConstant.checkNull(data.occupation_type)
                binding.tvName.text = AppConstant.checkNull(data.name)
                binding.tvAge.text = AppConstant.checkNull(data.age)
                binding.tvContactNumber.text = AppConstant.checkNull(data.contact_number)
                binding.tvEmail.text = AppConstant.checkNull(data.email)
                binding.tvAddress.text = AppConstant.checkNull(data.address)
                binding.tvCity.text = AppConstant.checkNull(data.city)
                binding.tvPostCode.text = AppConstant.checkNull(data.post_code)
            }
        }
    }
}