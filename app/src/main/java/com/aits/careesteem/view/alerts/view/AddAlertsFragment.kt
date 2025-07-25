package com.aits.careesteem.view.alerts.view


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogAddAlertConfirmBinding
import com.aits.careesteem.databinding.DialogConfirmExitBinding
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentAddAlertsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.DateTimeUtils
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.alerts.adapter.FileAdapter
import com.aits.careesteem.view.alerts.model.FileModel
import com.aits.careesteem.view.alerts.viewmodel.AddAlertsViewModel
import com.aits.careesteem.view.recyclerview.adapter.RecyclerArrayAdapter
import com.aits.careesteem.view.visits.model.VisitListResponse
import com.aits.careesteem.view.visits.viewmodel.VisitsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class AddAlertsFragment : Fragment() {
    private var _binding: FragmentAddAlertsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: AddAlertsViewModel by viewModels()
    private val visitViewModel: VisitsViewModel by activityViewModels()

    private var fileList: MutableList<FileModel> = mutableListOf()

    private lateinit var fileAdapter: FileAdapter

    private val severityList = listOf("Low", "Medium", "High")

    private var clientId: String = "-1"
    private var visitDetailsId: String = "-1"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getClientsList(requireActivity())
        //viewModel.getVisits(requireActivity())
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        if (visitViewModel.visitsList.value.isNullOrEmpty()) {
            visitViewModel.getVisits(requireActivity(), currentDate.format(formatter))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddAlertsBinding.inflate(inflater, container, false)
        setupOnBackPressed()
        setupAdapter()
        setupWidgets()
        setupViewModel()
        return binding.root
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showConfirmExitPopup()
                }
            }
        )
    }

    private fun setupAdapter() {
        fileAdapter = FileAdapter(requireContext(), fileList) { position ->
            // Remove item from the list and notify the adapter
            fileList.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
        }
        binding.apply {
            recyclerView.adapter = fileAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showConfirmExitPopup() {
        val dialog = Dialog(requireContext())
        val binding: DialogConfirmExitBinding =
            DialogConfirmExitBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Handle button clicks
        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            findNavController().popBackStack()
        }
        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupWidgets() {
        binding.btnCancel.setOnClickListener {
            showConfirmExitPopup()
        }

        binding.visitNotes.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            if (event.action == MotionEvent.ACTION_UP) {
                v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        binding.btnSave.setOnClickListener {
            if (clientId == "-1" || binding.clientName.text == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select client", ToastyType.WARNING)
                return@setOnClickListener
            }

            println(visitDetailsId)
            println(binding.visitName.text)

            if (visitDetailsId == "-1" || binding.visitName.text == "Select") {
                AlertUtils.showToast(requireActivity(), "Please select visits", ToastyType.WARNING)
                return@setOnClickListener
            }

            if (binding.severityOfConcern.text.isNullOrEmpty() || binding.severityOfConcern.text == "Select") {
                AlertUtils.showToast(
                    requireActivity(),
                    "Please select severity of concerns",
                    ToastyType.WARNING
                )
                return@setOnClickListener
            }

            if (binding.visitNotes.text.isNullOrEmpty()) {
                AlertUtils.showToast(
                    requireActivity(),
                    "Please enter concern details",
                    ToastyType.WARNING
                )
                return@setOnClickListener
            }

            if (binding.bmEnable.isChecked && fileList.isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please add body maps", ToastyType.WARNING)
                return@setOnClickListener
            }

            confirmAlerts { isConfirmed ->
                if (isConfirmed) {
                    viewModel.addAlerts(
                        requireActivity(),
                        clientId,
                        visitDetailsId,
                        binding.severityOfConcern.text.toString(),
                        binding.visitNotes.text.toString(),
                        fileList
                    )
                } else {
                    // Do something on negative
                }
            }
        }

        binding.bmDisable.isChecked = true
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Check which radio button is selected
            when (checkedId) {
                R.id.bmDisable -> {
                    fileList.clear()
                    fileAdapter.notifyDataSetChanged()
                    binding.apply {
                        bmDisable.setTextColor(requireContext().getColor(R.color.colorPrimary))
                        bmEnable.setTextColor(requireContext().getColor(R.color.black))
                        recyclerView.visibility = View.GONE
                        bodyMapController.visibility = View.GONE
                    }
                }

                R.id.bmEnable -> {
                    binding.apply {
                        bmDisable.setTextColor(requireContext().getColor(R.color.black))
                        bmEnable.setTextColor(requireContext().getColor(R.color.colorPrimary))
                        recyclerView.visibility = View.VISIBLE
                        bodyMapController.visibility = View.VISIBLE
                    }
                }
            }
        }

        val maxHeightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            200f, // max height in dp
            resources.displayMetrics
        ).toInt()

        binding?.let { safeBinding ->
            val recyclerView = safeBinding.rvClientName

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


        binding.clientName.setOnClickListener {
            //binding.allClientNameSpinner.performClick()

            // check visitViewModel.visitsList have value or not (Based on visits only we showing clients name)
            if (visitViewModel.visitsList.value!!.isEmpty()) {
                AlertUtils.showToast(
                    requireActivity(),
                    "No visits are available",
                    ToastyType.WARNING
                )
                return@setOnClickListener
            }

            if (binding.rvClientName.isVisible) {
                binding.rvClientName.visibility = View.GONE
                binding.clientName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_down),
                    null
                )
            } else {
                binding.rvClientName.visibility = View.VISIBLE
                binding.clientName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_up),
                    null
                )
            }
        }

        binding.visitName.setOnClickListener {
            if (clientId == "-1") {
                AlertUtils.showToast(
                    requireActivity(),
                    "Please select a client",
                    ToastyType.WARNING
                )
                return@setOnClickListener
            }

            //binding.allVisitTimeSpinner.performClick()
            if (binding.rvVisitName.isVisible) {
                binding.rvVisitName.visibility = View.GONE
                binding.visitName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_down),
                    null
                )
            } else {
                binding.rvVisitName.visibility = View.VISIBLE
                binding.visitName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_up),
                    null
                )
            }
        }

//        val adapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityList)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        binding.severityOfConcernSpinner.adapter = adapter
//
//        binding.severityOfConcernSpinner.onItemSelectedListener =
//            object : AdapterView.OnItemSelectedListener {
//                @SuppressLint("SetTextI18n")
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View,
//                    position: Int,
//                    id: Long
//                ) {
//                    binding.severityOfConcern.text = adapter.getItem(position).toString()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                }
//            }
//
//        binding.severityOfConcern.setOnClickListener {
//            binding.severityOfConcernSpinner.performClick()
//        }

        val adapter = RecyclerArrayAdapter(severityList) { selected ->
            binding.severityOfConcern.text = selected
            binding.rvConcern.visibility = View.GONE
            binding.severityOfConcern.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                requireContext().getDrawable(R.drawable.round_arrow_down),
                null
            )
        }

        binding.rvConcern.layoutManager = LinearLayoutManager(requireContext())
        binding.rvConcern.adapter = adapter

        binding.severityOfConcern.setOnClickListener {
            if (binding.rvConcern.isVisible) {
                binding.rvConcern.visibility = View.GONE
                binding.severityOfConcern.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_down),
                    null
                )
            } else {
                binding.rvConcern.visibility = View.VISIBLE
                binding.severityOfConcern.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.round_arrow_up),
                    null
                )
            }
        }

        binding.apply {
            bodyName.setOnClickListener {
                // if visible means in visible
                if (bodyName.tag == "Invisible") bodyName.tag = "Visible" else bodyName.tag =
                    "Invisible"
                faceName.tag = "Invisible"
                handName.tag = "Invisible"
                pelvisName.tag = "Invisible"
                feetName.tag = "Invisible"
                callWholeMethods()
            }

            layoutBodyFront.setOnClickListener {
                showBodyMapDialog(
                    "Body",
                    "Body Front",
                    requireContext().getDrawable(R.drawable.comp_body_front)
                )
            }

            layoutBodyBack.setOnClickListener {
                showBodyMapDialog(
                    "Body",
                    "Body Back",
                    requireContext().getDrawable(R.drawable.comp_body_back)
                )
            }

            faceName.setOnClickListener {
                bodyName.tag = "Invisible"
                if (faceName.tag == "Invisible") faceName.tag = "Visible" else faceName.tag =
                    "Invisible"
                handName.tag = "Invisible"
                pelvisName.tag = "Invisible"
                feetName.tag = "Invisible"
                callWholeMethods()
            }

            layoutFaceFront.setOnClickListener {
                showBodyMapDialog(
                    "Face",
                    "Face Front",
                    requireContext().getDrawable(R.drawable.comp_face_front)
                )
            }

            layoutFaceBack.setOnClickListener {
                showBodyMapDialog(
                    "Face",
                    "Face Back",
                    requireContext().getDrawable(R.drawable.comp_face_back)
                )
            }

            handName.setOnClickListener {
                bodyName.tag = "Invisible"
                faceName.tag = "Invisible"
                if (handName.tag == "Invisible") handName.tag = "Visible" else handName.tag =
                    "Invisible"
                pelvisName.tag = "Invisible"
                feetName.tag = "Invisible"
                callWholeMethods()
            }

            layoutRightFront.setOnClickListener {
                showBodyMapDialog(
                    "Hand",
                    "Right Front",
                    requireContext().getDrawable(R.drawable.comp_hand_right_front)
                )
            }

            layoutRightBack.setOnClickListener {
                showBodyMapDialog(
                    "Hand",
                    "Right Back",
                    requireContext().getDrawable(R.drawable.comp_hand_right_back)
                )
            }

            layoutLeftFront.setOnClickListener {
                showBodyMapDialog(
                    "Hand",
                    "Left Front",
                    requireContext().getDrawable(R.drawable.comp_hand_left_front)
                )
            }

            layoutLeftBack.setOnClickListener {
                showBodyMapDialog(
                    "Hand",
                    "Left Back",
                    requireContext().getDrawable(R.drawable.comp_hand_left_back)
                )
            }

            pelvisName.setOnClickListener {
                bodyName.tag = "Invisible"
                faceName.tag = "Invisible"
                handName.tag = "Invisible"
                if (pelvisName.tag == "Invisible") pelvisName.tag = "Visible" else pelvisName.tag =
                    "Invisible"
                feetName.tag = "Invisible"
                callWholeMethods()
            }

            layoutPelvisFront.setOnClickListener {
                showBodyMapDialog(
                    "Pelvis",
                    "Pelvis Front",
                    requireContext().getDrawable(R.drawable.comp_pelvis_front)
                )
            }

            layoutPelvisBack.setOnClickListener {
                showBodyMapDialog(
                    "Pelvis",
                    "Pelvis Back",
                    requireContext().getDrawable(R.drawable.comp_pelvis_back)
                )
            }

            feetName.setOnClickListener {
                bodyName.tag = "Invisible"
                faceName.tag = "Invisible"
                handName.tag = "Invisible"
                pelvisName.tag = "Invisible"
                if (feetName.tag == "Invisible") feetName.tag = "Visible" else feetName.tag =
                    "Invisible"
                callWholeMethods()
            }

            layoutRightFrontFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Right Front",
                    requireContext().getDrawable(R.drawable.comp_feet_right_front)
                )
            }

            layoutRightBackFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Right Back",
                    requireContext().getDrawable(R.drawable.comp_feet_right_back)
                )
            }

            layoutRightHeelFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Right Heel",
                    requireContext().getDrawable(R.drawable.comp_feet_right_heel)
                )
            }

            layoutLeftFrontFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Left Front",
                    requireContext().getDrawable(R.drawable.comp_feet_left_front)
                )
            }

            layoutLeftBackFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Left Back",
                    requireContext().getDrawable(R.drawable.comp_feet_left_back)
                )
            }

            layoutLeftHeelFeet.setOnClickListener {
                showBodyMapDialog(
                    "Feet",
                    "Left Heel",
                    requireContext().getDrawable(R.drawable.comp_feet_left_heel)
                )
            }
        }

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun callWholeMethods() {
        binding.apply {
            if (bodyName.tag == "Visible") {
                bodyName.tag = "Visible"
                bodyName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up),
                    null
                )
                bodyItemLayout.visibility = View.VISIBLE
            } else {
                bodyName.tag = "Invisible"
                bodyName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down),
                    null
                )
                bodyItemLayout.visibility = View.GONE
            }

            if (faceName.tag == "Visible") {
                faceName.tag = "Visible"
                faceName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up),
                    null
                )
                faceItemLayout.visibility = View.VISIBLE
            } else {
                faceName.tag = "Invisible"
                faceName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down),
                    null
                )
                faceItemLayout.visibility = View.GONE
            }

            if (handName.tag == "Visible") {
                handName.tag = "Visible"
                handName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up),
                    null
                )
                handItemLayout.visibility = View.VISIBLE
            } else {
                handName.tag = "Invisible"
                handName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down),
                    null
                )
                handItemLayout.visibility = View.GONE
            }

            if (pelvisName.tag == "Visible") {
                pelvisName.tag = "Visible"
                pelvisName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up),
                    null
                )
                pelvisItemLayout.visibility = View.VISIBLE
            } else {
                pelvisName.tag = "Invisible"
                pelvisName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down),
                    null
                )
                pelvisItemLayout.visibility = View.GONE
            }

            if (feetName.tag == "Visible") {
                feetName.tag = "Visible"
                feetName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_up),
                    null
                )
                feetItemLayout.visibility = View.VISIBLE
            } else {
                feetName.tag = "Invisible"
                feetName.setCompoundDrawablesWithIntrinsicBounds(
                    null,
                    null,
                    requireContext().getDrawable(R.drawable.ic_keyboard_arrow_down),
                    null
                )
                feetItemLayout.visibility = View.GONE
            }
        }
    }

    private fun showBodyMapDialog(bodyPartType: String, bodyPartName: String, image: Drawable?) {
        val bitmap = (image as? BitmapDrawable)?.bitmap
//        bitmap?.let {
//            val dialog = BodyMapMarksDialog.newInstance(
//                bodyPartType = bodyPartType,
//                bodyPartName = bodyPartName,
//                bitmap = it // Pass the actual bitmap
//            )
//
//            dialog.setBodyMapDialogListener(object : BodyMapMarksDialog.BodyMapDialogListener {
//                override fun onBodyMapSaved(bodyPartType: String, bodyPartName: String, file: File) {
//                    // Handle the saved file here
//                    AlertUtils.showLog("BodyMap", "Saved file path: ${file.path}")
//                    file.let {
//                        fileList.add(FileModel(bodyPartType, bodyPartName, it.name, it.path))
//                        fileAdapter.notifyDataSetChanged()
//                    } ?: run {
//                        AlertUtils.showToast(requireActivity(), "Something went wrong")
//                    }
//                }
//            })
//
//            dialog.show(childFragmentManager, "BodyMapDialog")
//        }
        val dialog = BodyMapMarksDialog.newInstance(bodyPartType, bodyPartName, bitmap!!)
        dialog.setBodyMapDialogListener(object : BodyMapMarksDialog.BodyMapDialogListener {
            override fun onBodyMapSaved(bodyPartType: String, bodyPartName: String, file: File) {
                fileList.add(FileModel(bodyPartType, bodyPartName, file.name, file.path))
                fileAdapter.notifyDataSetChanged()
            }
        })
        dialog.show(childFragmentManager, "BodyMapDialog")

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        viewModel.alertAdded.observe(viewLifecycleOwner) { success ->
            if (success) {
                findNavController().popBackStack()
            }
        }

        // Observe and populate client names in spinner
        viewModel.clientsList.observe(viewLifecycleOwner) { clients ->
            if (!clients.isNullOrEmpty()) {
                // Get distinct client names
                val uniqueClientNames = clients.map { it.clientName }
                    .distinct()
                    .toMutableList() // Convert to mutable list

                //uniqueClientNames.add(0, "Select")


                // Setup adapter for client name spinner
                val clientNameAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    uniqueClientNames
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                val adapter = RecyclerArrayAdapter(uniqueClientNames) { selected ->
                    binding.clientName.text = selected

                    // Match selected name to client object
                    val selectedClient = clients.find { it.clientName == selected }
                    clientId = selectedClient?.clientId ?: "-1"

                    // Reset previous selection
                    visitDetailsId = "-1"
                    binding.visitName.text = ""

                    // Filter and display visits for the selected client
                    val filteredVisits = visitViewModel.visitsList.value
                        ?.filter { it.clientId == clientId }
                        .orEmpty()

//                    val visitTimeOptions = filteredVisits.map { visit ->
//                        if (visit.plannedStartTime.isNotEmpty() && visit.plannedEndTime.isNotEmpty()) {
//                            "${visit.plannedStartTime} - ${visit.plannedEndTime}"
//                        } else if (visit.actualStartTime.isNotEmpty() && visit.actualStartTime[0].isNotEmpty() && visit.actualEndTime.isNotEmpty() && visit.actualEndTime[0].isNotEmpty()) {
//                            "${visit.actualStartTime[0]} (Unscheduled)"
//                        } else if (visit.actualStartTime[0].isNotEmpty() && visit.actualEndTime[0].isNotEmpty()) {
//                            "${visit.actualStartTime[0]} - ${visit.actualEndTime[0]}"
//                        } else {
//                            "N/A"
//                        }
//                    }.distinct().toMutableList() // Convert to mutable list

                    val visitTimeOptions = filteredVisits.map { visit ->
                        val plannedStart = visit.plannedStartTime
                        val plannedEnd = visit.plannedEndTime
                        val actualStart = visit.actualStartTime.firstOrNull()
                        val actualEnd = visit.actualEndTime.firstOrNull()

                        when {
                            plannedStart.isNotEmpty() && plannedEnd.isNotEmpty() -> {
                                "$plannedStart - $plannedEnd"
                            }
                            actualStart != null && actualStart.isNotEmpty() &&
                                    (actualEnd == null || actualEnd.isEmpty()) -> {
                                "${DateTimeUtils.convertTime(actualStart)} (Unscheduled)"
                            }
                            actualStart != null && actualEnd != null &&
                                    actualStart.isNotEmpty() && actualEnd.isNotEmpty() -> {
                                "${DateTimeUtils.convertTime(actualStart)} - ${DateTimeUtils.convertTime(actualEnd)}"
                            }
                            else -> {
                                "N/A"
                            }
                        }
                    }.distinct().toMutableList()

                    val adapter = RecyclerArrayAdapter(visitTimeOptions) { selected ->
                        binding.visitName.text = selected
                        val position = visitTimeOptions.indexOf(selected)
                        visitDetailsId = filteredVisits.getOrNull(position)?.visitDetailsId ?: "-1"
                        binding.rvVisitName.visibility = View.GONE
                        binding.visitName.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            requireContext().getDrawable(R.drawable.round_arrow_down),
                            null
                        )
                    }

                    binding.rvVisitName.layoutManager = LinearLayoutManager(requireContext())
                    binding.rvVisitName.adapter = adapter

                    binding.rvClientName.visibility = View.GONE

                    binding.clientName.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        requireContext().getDrawable(R.drawable.round_arrow_down),
                        null
                    )
                }

                binding.rvClientName.layoutManager = LinearLayoutManager(requireContext())
                binding.rvClientName.adapter = adapter

//                binding.allClientNameSpinner.adapter = clientNameAdapter
//
//                binding.allClientNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    @SuppressLint("SetTextI18n")
//                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                        val selectedClientName = clientNameAdapter.getItem(position) ?: return
//                        binding.clientName.text = selectedClientName
//
//                        // Match selected name to client object
//                        val selectedClient = clients.find { it.clientName == selectedClientName }
//                        clientId = selectedClient?.clientId ?: "-1"
//
//                        // Reset previous selection
//                        visitDetailsId = "-1"
//                        binding.visitName.text = ""
//
//                        // Filter and display visits for the selected client
//                        val filteredVisits = visitViewModel.visitsList.value
//                            ?.filter { it.clientId == clientId }
//                            .orEmpty()
//
//                        val visitTimeOptions = filteredVisits.map { visit ->
//                            if(visit.plannedStartTime.isNotEmpty() && visit.plannedEndTime.isNotEmpty()) {
//                                "${visit.plannedStartTime} - ${visit.plannedEndTime}"
//                            } else if(visit.actualStartTime[0].isNotEmpty() && visit.actualEndTime[0].isEmpty()) {
//                                "${visit.actualStartTime[0]} (Unscheduled)"
//                            } else if(visit.actualStartTime[0].isNotEmpty() && visit.actualEndTime[0].isNotEmpty()) {
//                                "${visit.actualStartTime[0]} - ${visit.actualEndTime[0]}"
//                            } else {
//                                "N/A"
//                            }
//                        }.distinct().toMutableList() // Convert to mutable list
//
//                        //visitTimeOptions.add(0, "Select")
//
//                        val visitTimeAdapter = ArrayAdapter(
//                            requireContext(),
//                            android.R.layout.simple_spinner_item,
//                            visitTimeOptions
//                        ).apply {
//                            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                        }
//
//                        binding.allVisitTimeSpinner.adapter = visitTimeAdapter
//
//                        binding.allVisitTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                            @SuppressLint("SetTextI18n")
//                            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                                binding.visitName.text = visitTimeOptions[position]
//                                visitDetailsId = filteredVisits.getOrNull(position)?.visitDetailsId ?: "-1"
//                                // Skip setting ID for "Select"
////                                visitDetailsId = if (position > 0) {
////                                    filteredVisits.getOrNull(position - 1)?.visitDetailsId ?: "-1"
////                                } else {
////                                    "-1"
////                                }
//                            }
//
//                            override fun onNothingSelected(parent: AdapterView<*>?) {
//                                // No action required
//                            }
//                        }
//                    }
//
//                    override fun onNothingSelected(parent: AdapterView<*>?) {
//                        // No action required
//                    }
//                }
            }
        }

        // Data visibility
        viewModel.filterVisitsList.observe(viewLifecycleOwner) { data ->
            if (data.isNotEmpty()) {
                val spinnerList = ArrayList<String>()
                for (client in data) {
                    spinnerList.add("${client.plannedStartTime} - ${client.plannedEndTime}")
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    spinnerList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                binding.allVisitTimeSpinner.adapter = adapter
//
//                binding.allVisitTimeSpinner.onItemSelectedListener =
//                    object : AdapterView.OnItemSelectedListener {
//                        @SuppressLint("SetTextI18n")
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View,
//                            position: Int,
//                            id: Long
//                        ) {
//                            binding.visitName.text = adapter.getItem(position).toString()
//                            visitDetailsId = data[position].visitDetailsId
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                        }
//                    }
            } else {
                visitDetailsId = "-1"
                binding.visitName.text = ""
            }
        }
    }

    private fun confirmAlerts(onConfirm: (Boolean) -> Unit) {
        val dialog = Dialog(requireContext()).apply {
            val binding = DialogAddAlertConfirmBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCancelable(false)

            binding.btnPositive.setOnClickListener {
                dismiss()
                onConfirm(true)
            }

            binding.btnNegative.setOnClickListener {
                dismiss()
                onConfirm(false)
            }

            window?.setBackgroundDrawableResource(android.R.color.transparent)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setDimAmount(0.8f)
        }
        dialog.show()
    }
}