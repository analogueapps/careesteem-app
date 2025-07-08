package com.aits.careesteem.view.clients.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ItemAttachedDocumentsBinding
import com.aits.careesteem.databinding.ItemUploadedDocumentsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.clients.model.UploadedDocumentsResponse
import com.google.android.material.bottomsheet.BottomSheetDialog

class DocumentsAdapter(
    private val context: Context,
    private val adapterList: List<UploadedDocumentsResponse.Data>,
    private val documentClickListener: OnDocumentClickListener,
    private val dialog: BottomSheetDialog?
) : RecyclerView.Adapter<DocumentsAdapter.AlertViewHolder>() {

    private var expandedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding =
            ItemUploadedDocumentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(adapterList[position], position)
    }

    override fun getItemCount(): Int = adapterList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class AlertViewHolder(private val binding: ItemUploadedDocumentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
        fun bind(data: UploadedDocumentsResponse.Data, position: Int) = with(binding) {
            try {
                alertName.text = AppConstant.checkNull(data.document_name)

                val isExpanded = position == expandedPosition
                detailLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
                alertName.tag = if (isExpanded) "Visible" else "Invisible"
                val icon =
                    if (isExpanded) R.drawable.round_arrow_up else R.drawable.round_arrow_down
                alertName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    context.getDrawable(icon),
                    null
                )

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
                additionalInfo.text = AppConstant.checkNull(data.additional_info)

                // Setup nested image recycler
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter =
                    AttachDocumentAdapter(data.attach_document, documentClickListener, dialog)
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("DocumentsAdapter", "" + e.printStackTrace())
            }
        }
    }
}

class AttachDocumentAdapter(
    private val documentList: List<UploadedDocumentsResponse.Data.AttachDocument>,
    private val documentClickListener: OnDocumentClickListener,
    private val dialog: BottomSheetDialog?
) : RecyclerView.Adapter<AttachDocumentAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            ItemAttachedDocumentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(documentList[position])
    }

    override fun getItemCount(): Int = documentList.size

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    inner class ImageViewHolder(private val binding: ItemAttachedDocumentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: UploadedDocumentsResponse.Data.AttachDocument) = with(binding) {
            try {
                binding.apply {
                    alertName.text = AppConstant.checkNull(data.filename)

                    viewFile.setOnClickListener {
                        documentClickListener.onDocumentClicked(data.url, dialog)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AlertUtils.showLog("AttachDocumentAdapter", "" + e.printStackTrace())
            }
        }
    }
}

interface OnDocumentClickListener {
    fun onDocumentClicked(url: String, dialog: BottomSheetDialog?)
}
