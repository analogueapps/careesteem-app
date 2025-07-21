package com.aits.careesteem.view.bottomsheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.aits.careesteem.databinding.DialogTodoEditBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.view.visits.model.TodoListResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TodoBottomSheetFragment: BottomSheetDialogFragment() {

    companion object {
        const val TAG = "TodoBottomSheetFragment"

        private const val ARG_DATA = "arg_data"
        private const val ARG_ID = "arg_id"

        fun newInstance(data: TodoListResponse.Data, id: String?): TodoBottomSheetFragment {
            val args = Bundle().apply {
                putSerializable(ARG_DATA, data)
                putString(ARG_ID, id)
            }
            return TodoBottomSheetFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogTodoEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var data: TodoListResponse.Data
    private var visitId: String = ""

    private var todoUpdateListener: OnTodoUpdateListener? = null

    interface OnTodoUpdateListener {
        fun onTodoUpdated(
            notes: String,
            data: TodoListResponse.Data,
            todoOutCome: Int,
            visitDetailsId: String
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when {
            context is OnTodoUpdateListener -> todoUpdateListener = context
            parentFragment is OnTodoUpdateListener -> todoUpdateListener = parentFragment as OnTodoUpdateListener
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

//        val displayMetrics = Resources.getSystem().displayMetrics
//        val screenHeight = displayMetrics.heightPixels
//        val maxHeight = (screenHeight * 0.85).toInt()
//
//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            maxHeight
//        )

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
        _binding = DialogTodoEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = arguments?.getSerializable(ARG_DATA) as TodoListResponse.Data
        visitId = arguments?.getString(ARG_ID).orEmpty()

        setupUI()
    }

    @SuppressLint("UseCompatLoadingForDrawables", "ClickableViewAccessibility")
    private fun setupUI() = with(binding) {
        closeButton.setOnClickListener { dismiss() }
        carerNotes.movementMethod = ScrollingMovementMethod.getInstance()
//        carerNotes.setOnTouchListener { v, event ->
//            v.parent.requestDisallowInterceptTouchEvent(true)
//            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//                v.parent.requestDisallowInterceptTouchEvent(false)
//            }
//            false
//        }
        carerNotes.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if (event.action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        //todoTitle.text = AppConstant.checkNull(data.todoName)
        todoName.text = AppConstant.checkNull(data.todoName)
        val addNote = AppConstant.checkNull(data.additionalNotes)
        additionalNotes.text = addNote
        if (addNote != "N/A") {
            addNoteMain.visibility = View.VISIBLE
        }

        carerNotes.setText(data.carerNotes)

        btnCompleted.setOnClickListener {
            dismiss()
            //updateTodoStatus(data, 1, carerNotes.text.toString().trim())
            todoUpdateListener?.onTodoUpdated(carerNotes.text.toString().trim(), data, 1, visitId)
        }

        btnNotCompleted.setOnClickListener {
            dismiss()
            //updateTodoStatus(data, 0, carerNotes.text.toString().trim())
            todoUpdateListener?.onTodoUpdated(carerNotes.text.toString().trim(), data, 0, visitId)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        todoUpdateListener = null
        super.onDetach()
    }
}
