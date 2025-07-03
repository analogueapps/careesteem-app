package com.aits.careesteem.view.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogMedicationUpdateBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.BodyMapImageAdapter
import com.aits.careesteem.view.alerts.adapter.BodyMapItem
import com.aits.careesteem.view.recyclerview.adapter.MedicationStatusAdapter
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MedicationBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "MedicationBottomSheetFragment"

        private const val ARG_DATA = "arg_data"
        private const val ARG_ID = "arg_id"

        fun newInstance(data: MedicationDetailsListResponse.Data, id: String?): MedicationBottomSheetFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATA, data)
                putString(ARG_ID, id)
            }
            return MedicationBottomSheetFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogMedicationUpdateBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: MedicationDetailsListResponse.Data
    private var visitId: String = ""
    private var action: Int = 0

    private var medicationUpdateListener: OnMedicationUpdateListener? = null

    interface OnMedicationUpdateListener {
        fun onMedicationUpdated(
            status: String,
            notes: String,
            data: MedicationDetailsListResponse.Data,
            visitDetailsId: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is OnMedicationUpdateListener -> medicationUpdateListener = context
            parentFragment is OnMedicationUpdateListener -> medicationUpdateListener = parentFragment as OnMedicationUpdateListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.layoutParams?.height =
                (resources.displayMetrics.heightPixels * 0.75).toInt()
        }
        dialog.window?.setDimAmount(0.8f)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMedicationUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = arguments?.getSerializable(ARG_DATA) as MedicationDetailsListResponse.Data
        visitId = arguments?.getString(ARG_ID).orEmpty()

        setupUI()
    }

    private fun setupUI() = with(binding) {
        closeButton.setOnClickListener { dismiss() }

        nhsMedicineName.text = data.nhs_medicine_name
        medicationType.text = data.medication_type
        medicationSupport.text = data.medication_support
        doseQty.text = data.quantity_each_dose.toString()
        medicationRoute.text = data.medication_route_name

        frequencyMedication.text = when (data.medication_type) {
            "PRN" -> "${data.doses} Doses per ${data.dose_per} ${data.time_frame}"
            else -> data.day_name
        }

        medicationStatus.text = if (data.status == "Scheduled") "Select" else data.status

        val statuses = AppConstant.getNewStatuses(requireContext())
        val adapter = MedicationStatusAdapter(statuses) { selected ->
            medicationStatus.text = selected
            rvStatus.visibility = View.GONE
        }

        rvStatus.layoutManager = LinearLayoutManager(requireContext())
        rvStatus.adapter = adapter

        medicationStatus.setOnClickListener {
            rvStatus.visibility = if (rvStatus.isVisible) View.GONE else View.VISIBLE
        }

        if (!data.body_part_names.isNullOrEmpty() && !data.body_map_image_url.isNullOrEmpty()) {
            tvBodyMap.visibility = View.VISIBLE
            recyclerView.visibility = View.VISIBLE

            val bodyMapItems = data.body_part_names.mapIndexed { index, name ->
                BodyMapItem(
                    partName = name,
                    imageUrl = data.body_map_image_url.getOrNull(index).orEmpty()
                )
            }

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = BodyMapImageAdapter(requireContext(), bodyMapItems)
        } else {
            tvBodyMap.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }

        btnSave.setOnClickListener {
            val status = medicationStatus.text.toString()
            val notes = medicationNotes.text.toString().trim()

            if (status == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select status", ToastyType.WARNING)
                return@setOnClickListener
            }

//            if (notes.isEmpty()) {
//                AlertUtils.showToast(requireActivity(), "Please enter notes", ToastyType.WARNING)
//                return@setOnClickListener
//            }

            dismiss()

            medicationUpdateListener?.onMedicationUpdated(status, notes, data, visitId)
        }

        btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        medicationUpdateListener = null
        super.onDetach()
    }
}
