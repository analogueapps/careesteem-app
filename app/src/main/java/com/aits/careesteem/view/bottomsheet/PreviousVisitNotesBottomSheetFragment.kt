package com.aits.careesteem.view.bottomsheet

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.databinding.DialogPreviousAlertsBinding
import com.aits.careesteem.view.visits.adapter.VisitNotesAdapter
import com.aits.careesteem.view.visits.model.ClientVisitNotesDetails
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreviousVisitNotesBottomSheetFragment : BottomSheetDialogFragment(), VisitNotesAdapter.OnItemItemClick {

    companion object {
        const val TAG = "PreviousVisitNotesBottomSheetFragment"

        private const val ARG_DATA = "arg_data"

        fun newInstance(data: String?): PreviousVisitNotesBottomSheetFragment {
            val args = Bundle().apply {
                putString(ARG_DATA, data)
            }
            return PreviousVisitNotesBottomSheetFragment().apply {
                arguments = args
            }
        }
    }

    private var _binding: DialogPreviousAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var visitNotesAdapter: VisitNotesAdapter

    private var visitNote: String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
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
        _binding = DialogPreviousAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visitNote = arguments?.getString(ARG_DATA).orEmpty()
        // Convert back from string to list
        val gson = Gson()
        val type = object : TypeToken<List<ClientVisitNotesDetails.Data>>() {}.type
        val convertedList: List<ClientVisitNotesDetails.Data> = gson.fromJson(visitNote, type)

        visitNotesAdapter = VisitNotesAdapter(requireContext(),  this@PreviousVisitNotesBottomSheetFragment,
            showOnly = true,
            isChanges = false
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = visitNotesAdapter
        }

        setupUI(convertedList)
    }

    private fun setupUI(convertedList: List<ClientVisitNotesDetails.Data>) = with(binding) {

        closeButton.setOnClickListener { dismiss() }
        visitNotesAdapter.updateList(convertedList)

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onItemItemClicked(data: ClientVisitNotesDetails.Data) {

    }
}