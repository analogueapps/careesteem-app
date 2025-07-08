package com.aits.careesteem.view.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.aits.careesteem.databinding.DialogVisitNotesBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.ToastyType
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class VisitNotesBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "VisitNotesBottomSheetFragment"

        private const val ARG_DATA = "arg_data"
        private const val ARG_ID = "arg_id"
        private const val ARG_ACTION = "arg_action"
        private const val ARG_VISIT_ID = "arg_visit_id"
        private const val ARG_CREATE_USER_ID = "arg_create_user_id"

        fun newInstance(data: String?, id: String?, action: Int, visitId: String?, createdByUserid: String?): VisitNotesBottomSheetFragment {
            val args = Bundle().apply {
                putString(ARG_DATA, data)
                putString(ARG_ID, id)
                putInt(ARG_ACTION, action)
                putString(ARG_VISIT_ID, visitId)
                putString(ARG_CREATE_USER_ID, createdByUserid)
            }
            return VisitNotesBottomSheetFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogVisitNotesBinding? = null
    private val binding get() = _binding!!

    private var visitNote: String = ""
    private var visitId: String = ""
    private var action: Int = 0
    private var visitNotesId: String = ""
    private var createdByUserid: String = ""

    private var visitNoteUpdateListener: OnVisitNotesUpdateListener? = null

    interface OnVisitNotesUpdateListener {
        fun onVisitNoteUpdated(
            visitNotes: String,
            visitDetailsId: String,
            visitNotesId: String,
            createdByUserid: String,
            action: Int
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is OnVisitNotesUpdateListener -> visitNoteUpdateListener = context
            parentFragment is OnVisitNotesUpdateListener -> visitNoteUpdateListener =
                parentFragment as OnVisitNotesUpdateListener
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
        _binding = DialogVisitNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitNote = arguments?.getString(ARG_DATA).orEmpty()
        visitId = arguments?.getString(ARG_ID).orEmpty()
        action = arguments?.getInt(ARG_ACTION) ?: 0
        visitNotesId = arguments?.getString(ARG_VISIT_ID).orEmpty()
        createdByUserid = arguments?.getString(ARG_CREATE_USER_ID).orEmpty()

        setupUI()
    }

    private fun setupUI() = with(binding) {
        visitNotes.setText(visitNote)

        closeButton.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val notes = visitNotes.text.toString()

            if (binding.visitNotes.text.toString().isEmpty()) {
                AlertUtils.showToast(
                    requireActivity(),
                    "Please enter Visit notes",
                    ToastyType.WARNING
                )
                return@setOnClickListener
            }

            dismiss()

            visitNoteUpdateListener?.onVisitNoteUpdated(notes, visitId, visitNotesId, createdByUserid, action)
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
        visitNoteUpdateListener = null
        super.onDetach()
    }
}
