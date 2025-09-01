package com.aits.careesteem.view.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogMedicationUpdatePrnBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.BodyMapImageAdapter
import com.aits.careesteem.view.alerts.adapter.BodyMapItem
import com.aits.careesteem.view.recyclerview.adapter.MedicationStatusAdapter
import com.aits.careesteem.view.visits.model.MedicationDetailsListResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MedicationPrnBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "MedicationPrnBottomSheet"
        const val ACTION_STATUS_UPDATE = 0
        const val ACTION_MEDICATION_UPDATE = 1
        private const val ARG_DATA = "arg_data"
        private const val ARG_ID = "arg_id"
        private const val ARG_ACTION = "arg_action"

        fun newInstance(data: MedicationDetailsListResponse.Data, id: String?, action: Int): MedicationPrnBottomSheetFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATA, data)
                putString(ARG_ID, id)
                putInt(ARG_ACTION, action)
            }
            return MedicationPrnBottomSheetFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogMedicationUpdatePrnBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: MedicationDetailsListResponse.Data
    private var visitId: String = ""
    private var action: Int = 0

    private var medicationUpdateListener: OnPrnMedicationUpdateListener? = null
    private var statusUpdateListener: OnPrnStatusUpdateListener? = null

    interface OnPrnMedicationUpdateListener {
        fun onPrnMedicationUpdated(
            status: String,
            notes: String,
            data: MedicationDetailsListResponse.Data,
            visitDetailsId: String
        )
    }

    interface OnPrnStatusUpdateListener {
        fun onPrnStatusChanged(
            status: String,
            notes: String,
            data: MedicationDetailsListResponse.Data,
            visitDetailsId: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is OnPrnMedicationUpdateListener -> medicationUpdateListener = context
            parentFragment is OnPrnMedicationUpdateListener -> medicationUpdateListener = parentFragment as OnPrnMedicationUpdateListener
        }

        when {
            context is OnPrnStatusUpdateListener -> statusUpdateListener = context
            parentFragment is OnPrnStatusUpdateListener -> statusUpdateListener = parentFragment as OnPrnStatusUpdateListener
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                // 85% of screen height
                val layoutParams = it.layoutParams
                layoutParams.height = (resources.displayMetrics.heightPixels * 0.85).toInt()
                it.layoutParams = layoutParams

                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
                behavior.skipCollapsed = true
            }
        }

        dialog.window?.setDimAmount(0.8f)
        // 💡 This is the fix for keyboard overlap
        //dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMedicationUpdatePrnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = arguments?.getSerializable(ARG_DATA) as MedicationDetailsListResponse.Data
        visitId = arguments?.getString(ARG_ID).orEmpty()
        action = arguments?.getInt(ARG_ACTION) ?: 0

        setupUI()
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    private fun setupUI() = with(binding) {
        closeButton.setOnClickListener { dismiss() }
        medicationNotes.movementMethod = ScrollingMovementMethod.getInstance()
        medicationNotes.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        nhsMedicineName.text = data.nhs_medicine_name
        medicationType.text = data.medication_type
        medicationSupport.text = data.medication_support
        doseQty.text = data.quantity_each_dose.toString()
        medicationRoute.text = data.medication_route_name
        medicationNotes.setText(data.carer_notes)
        val addNote = AppConstant.checkNull(data.additional_instructions)
        additionalNotes.text = addNote
        if (addNote != "N/A") {
            addNoteMain.visibility = View.VISIBLE
        }

        frequencyMedication.text = when (data.medication_type) {
            "PRN" -> "${data.dose_per} Doses per ${data.doses} ${data.time_frame}"
            else -> data.day_name
        }

        medicationStatus.text = if (data.status == "Scheduled") "Select" else data.status

        val statuses = AppConstant.getNewStatuses(requireContext())
        val adapter = MedicationStatusAdapter(statuses) { selected ->
            medicationStatus.text = selected
            rvStatus.visibility = View.GONE
            binding.medicationStatus.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                requireContext().getDrawable(R.drawable.round_arrow_down    ),
                null
            )
        }

        rvStatus.layoutManager = LinearLayoutManager(requireContext())
        rvStatus.adapter = adapter

        val maxHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            200f, // max height in dp
            resources.displayMetrics
        ).toInt()

        binding?.let { safeBinding ->
            val recyclerView = safeBinding.rvStatus

            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (view == null || !isAdded) return // Avoid if Fragment is detached

                    recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    if (recyclerView.height > maxHeightPx) {
                        recyclerView.layoutParams = recyclerView.layoutParams.apply {
                            height = maxHeightPx
                        }
                        recyclerView.requestLayout()
                    }
                }
            })
        }

        medicationStatus.setOnClickListener {
            //rvStatus.visibility = if (rvStatus.isVisible) View.GONE else View.VISIBLE
            if (binding.rvStatus.isVisible) {
                binding.rvStatus.visibility = View.GONE
                binding.medicationStatus.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_down),
                    null
                )
            } else {
                binding.rvStatus.visibility = View.VISIBLE
                binding.medicationStatus.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_up),
                    null
                )
            }
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
            //medicationNotes.setLines(5)
        } else {
            tvBodyMap.visibility = View.GONE
            recyclerView.visibility = View.GONE
            //medicationNotes.setLines(7)
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

            if (action == 0) {
                Log.d("BottomSheet", "Calling statusUpdateListener")
                statusUpdateListener?.onPrnStatusChanged(status, notes, data, visitId)
            } else {
                Log.d("BottomSheet", "Calling medicationUpdateListener")
                medicationUpdateListener?.onPrnMedicationUpdated(status, notes, data, visitId)
            }
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
        statusUpdateListener = null
        super.onDetach()
    }
}
