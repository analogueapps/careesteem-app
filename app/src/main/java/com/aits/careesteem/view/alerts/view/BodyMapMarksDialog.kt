package com.aits.careesteem.view.alerts.view

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogBodyMappingBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import java.io.File


class BodyMapMarksDialog : DialogFragment() {
    private lateinit var toolbar: Toolbar

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
            bitmap = it.getParcelable("bitmap") // Retrieve bitmap
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogBodyMappingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set image in ImageView
        bitmap?.let {
            binding.bodyMapView.setImageBitmap(it)
        }

        // Setup toolbar with navigation and menu items
        toolbar = view.findViewById(R.id.toolbar) // Assuming you have a toolbar in your dialog layout
        toolbar.apply {
            setNavigationOnClickListener { dismiss() } // Dismiss the dialog when the navigation button is clicked
            setTitle(arguments?.getString("bodyPartName") ?: "")
            setTitleTextColor(resources.getColor(R.color.black))
        }

        binding.btnUndo.setOnClickListener {
            binding.bodyMapView.undo()
        }

        binding.btnRedo.setOnClickListener {
            binding.bodyMapView.redo()
        }

        binding.btnSave.setOnClickListener {
            //dialog.dismiss() - Dialog will be dismissed by toolbar or menu item clicks
//            if (binding.bodyMapView.hasMarkers()) {
//                dismiss()
//                val bitmap = binding.bodyMapView.getBitmap()
//                val file = AppConstant.bitmapToFile(requireContext(), bitmap, "${System.currentTimeMillis()}.png")
//                AlertUtils.showLog("FilePath", "File saved at: ${file?.path}")
//
//                file?.let {
//                    listener?.onBodyMapSaved(
//                        arguments?.getString("bodyPartType") ?: "",
//                        arguments?.getString("bodyPartName") ?: "",
//                        it
//                    )
//                } ?: run {
//                    AlertUtils.showToast(requireActivity(), "Something went wrong")
//                }
//            } else {
//                //AlertUtils.showToast(requireActivity(), "Please add at least one marker before saving.")
//                AlertDialog.Builder(requireContext())
//                    .setTitle("Alert")
//                    .setMessage("Please add at least one marker before saving.")
//                    .setPositiveButton("Okay", null)
//                    .show()
//            }
            dismiss()
            val bitmap = binding.bodyMapView.getBitmap()
            val file = AppConstant.bitmapToFile(requireContext(), bitmap, "${System.currentTimeMillis()}.png")
            AlertUtils.showLog("FilePath", "File saved at: ${file?.path}")

            file?.let {
                listener?.onBodyMapSaved(
                    arguments?.getString("bodyPartType") ?: "",
                    arguments?.getString("bodyPartName") ?: "",
                    it
                )
            } ?: run {
                AlertUtils.showToast(requireActivity(), "Something went wrong")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(bodyPartType: String, bodyPartName: String, bitmap: Bitmap): BodyMapMarksDialog {
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
