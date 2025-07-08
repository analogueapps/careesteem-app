package com.aits.careesteem.view.alerts.view

import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.aits.careesteem.databinding.DialogBodyMappingBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ToastyType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class BodyMapMarksDialog : BottomSheetDialogFragment() {

    private var _binding: DialogBodyMappingBinding? = null
    private val binding get() = _binding!!

    private var listener: BodyMapDialogListener? = null

    interface BodyMapDialogListener {
        fun onBodyMapSaved(bodyPartType: String, bodyPartName: String, file: File)
    }

    fun setBodyMapDialogListener(listener: BodyMapDialogListener) {
        this.listener = listener
    }

    private var bodyPartType: String? = null
    private var bodyPartName: String? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            bodyPartType = it.getString("bodyPartType")
            bodyPartName = it.getString("bodyPartName")
            bitmap = it.getParcelable("bitmap")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = object : BottomSheetDialog(requireContext(), theme) {
            override fun onBackPressed() {
                // Prevent dismissing on back press
            }
        }

//        dialog.setOnShowListener { dlg ->
//            val d = dlg as BottomSheetDialog
//            val bottomSheet =
//                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            bottomSheet?.let {
//                val behavior = BottomSheetBehavior.from(it)
//                behavior.isDraggable = false
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                behavior.skipCollapsed = true
//            }
//
//            // ✅ Set dim amount to 0.8
//            d.window?.apply {
//                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//                setDimAmount(0.8f)
//            }
//        }

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
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogBodyMappingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bitmap?.let {
            binding.bodyMapView.setImageBitmap(it)
        }

        binding.toolbar.apply {
            title = bodyPartName ?: ""
            setNavigationOnClickListener { dismiss() }
        }

        binding.bodyPartName.text = bodyPartName ?: ""

        binding.closeButton.setOnClickListener {
            dismiss()
        }

        binding.btnUndo.setOnClickListener {
            binding.bodyMapView.undo()
        }

        binding.btnRedo.setOnClickListener {
            binding.bodyMapView.redo()
        }

        binding.btnSave.setOnClickListener {
            val bitmap = binding.bodyMapView.getBitmap()
            val file = AppConstant.bitmapToFile(
                requireContext(),
                bitmap,
                "${System.currentTimeMillis()}.png"
            )
            file?.let {
                listener?.onBodyMapSaved(bodyPartType ?: "", bodyPartName ?: "", it)
            } ?: run {
                AlertUtils.showToast(requireActivity(), "Something went wrong", ToastyType.ERROR)
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(
            bodyPartType: String,
            bodyPartName: String,
            bitmap: Bitmap
        ): BodyMapMarksDialog {
            return BodyMapMarksDialog().apply {
                arguments = Bundle().apply {
                    putString("bodyPartType", bodyPartType)
                    putString("bodyPartName", bodyPartName)
                    putParcelable("bitmap", bitmap)
                }
            }
        }
    }
}
