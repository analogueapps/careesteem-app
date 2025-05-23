package com.aits.careesteem.view.notification.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.databinding.ItemNotificationListBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.notification.model.NotificationListResponse

class NotificationAdapter(
    private val context: Context,
    private val onDeleteItemItemClick: OnDeleteItemItemClick
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    interface OnDeleteItemItemClick {
        fun onDeleteItemItemClicked(data: NotificationListResponse.Data)
    }

    private var adapterList = listOf<NotificationListResponse.Data>()

    fun updateList(list: List<NotificationListResponse.Data>) {
        adapterList = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemNotificationListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    inner class ViewHolder(private val binding: ItemNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: NotificationListResponse.Data) {
            binding.apply {
                val dateFormatted = AppConstant.alertsListTimer(data.created_at)
                notificationDate.text = dateFormatted
                notificationTitle.text = data.notification_title
                notificationMessage.text = data.notification_body

                btnDelete.setOnClickListener {
                    onDeleteItemItemClick.onDeleteItemItemClicked(data)
                }
            }
        }
    }
}