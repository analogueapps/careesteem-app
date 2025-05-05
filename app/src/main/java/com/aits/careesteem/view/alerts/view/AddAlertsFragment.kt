package com.aits.careesteem.view.alerts.view


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TimeUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogAboutClientBinding
import com.aits.careesteem.databinding.DialogBodyMappingBinding
import com.aits.careesteem.databinding.FragmentAddAlertsBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.view.alerts.adapter.FileAdapter
import com.aits.careesteem.view.alerts.model.FileModel
import com.aits.careesteem.view.alerts.viewmodel.AddAlertsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class AddAlertsFragment : Fragment() {
    private var _binding: FragmentAddAlertsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: AddAlertsViewModel by viewModels()

    private var fileList: MutableList<FileModel> = mutableListOf()

    private lateinit var fileAdapter: FileAdapter

    private val severityList = listOf("Low", "Medium", "High")

    private var clientId: Int = -1
    private var visitDetailsId: Int = -1

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getClientsList(requireActivity())
        viewModel.getVisits(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddAlertsBinding.inflate(inflater, container, false)
        setupAdapter()
        setupWidgets()
        setupViewModel()
        return binding.root
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupWidgets() {
        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            if (clientId == -1) {
                AlertUtils.showToast(requireActivity(), "Please select client")
                return@setOnClickListener
            }

            if (visitDetailsId == -1) {
                AlertUtils.showToast(requireActivity(), "Please select visits")
                return@setOnClickListener
            }

            if (binding.severityOfConcern.text.isNullOrEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please select severity of concerns")
                return@setOnClickListener
            }

            if (binding.visitNotes.text.isNullOrEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please enter concern details")
                return@setOnClickListener
            }

            if (binding.bmEnable.isChecked && fileList.isEmpty()) {
                AlertUtils.showToast(requireActivity(), "Please add body maps")
                return@setOnClickListener
            }

            viewModel.addAlerts(
                requireActivity(),
                clientId,
                visitDetailsId,
                binding.severityOfConcern.text.toString(),
                binding.visitNotes.text.toString(),
                fileList
            )
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

        binding.clientName.setOnClickListener {
            binding.allClientNameSpinner.performClick()
        }

        binding.visitName.setOnClickListener {
            if (clientId == -1) {
                AlertUtils.showToast(requireActivity(), "Please select a client")
                return@setOnClickListener
            }

            binding.allVisitTimeSpinner.performClick()
        }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, severityList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.severityOfConcernSpinner.adapter = adapter

        binding.severityOfConcernSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    binding.severityOfConcern.text = adapter.getItem(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        binding.severityOfConcern.setOnClickListener {
            binding.severityOfConcernSpinner.performClick()
        }

        binding.apply {
            bodyName.setOnClickListener {
                if (bodyName.tag == "Invisible") {
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
                if (faceName.tag == "Invisible") {
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
                if (handName.tag == "Invisible") {
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
                if (pelvisName.tag == "Invisible") {
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
                if (feetName.tag == "Invisible") {
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

    private fun showBodyMapDialog(bodyPartType: String, bodyPartName: String, image: Drawable?) {
        val bitmap = (image as? BitmapDrawable)?.bitmap
        bitmap?.let {
            val dialog = BodyMapMarksDialog.newInstance(
                bodyPartType = bodyPartType,
                bodyPartName = bodyPartName,
                bitmap = it // Pass the actual bitmap
            )

            dialog.setBodyMapDialogListener(object : BodyMapMarksDialog.BodyMapDialogListener {
                override fun onBodyMapSaved(bodyPartType: String, bodyPartName: String, file: File) {
                    // Handle the saved file here
                    AlertUtils.showLog("BodyMap", "Saved file path: ${file.path}")
                    file.let {
                        fileList.add(FileModel(bodyPartType, bodyPartName, it.name, it.path))
                        fileAdapter.notifyDataSetChanged()
                    } ?: run {
                        AlertUtils.showToast(requireActivity(), "Something went wrong")
                    }
                }
            })

            dialog.show(childFragmentManager, "BodyMapDialog")
        }

//        val dialog = BodyMapMarksDialog.newInstance(
//            bodyPartType = bodyPartType,
//            bodyPartName = bodyPartName,
//            imageResId = imageResId
//        )
//
//        dialog.setBodyMapDialogListener(object : BodyMapMarksDialog.BodyMapDialogListener {
//            override fun onBodyMapSaved(bodyPartType: String, bodyPartName: String, file: File) {
//                // Handle the saved file here
//                AlertUtils.showLog("BodyMap", "Saved file path: ${file.path}")
//                file.let {
//                    fileList.add(FileModel(bodyPartType, bodyPartName, it.name, it.path))
//                    fileAdapter.notifyDataSetChanged()
//                } ?: run {
//                    AlertUtils.showToast(requireActivity(), "Something went wrong")
//                }
//            }
//        })
//
//        dialog.show(childFragmentManager, "BodyMapDialog")
//        val dialog = Dialog(requireContext())
//        val binding: DialogBodyMappingBinding =
//            DialogBodyMappingBinding.inflate(layoutInflater)
//
//        dialog.setContentView(binding.root)
//        dialog.setCancelable(AppConstant.TRUE)
//
//        // Add data
//        binding.bodyMapView.setImageDrawable(image)
//
//        binding.btnUndo.setOnClickListener {
//            binding.bodyMapView.undo()
//        }
//
//        binding.btnRedo.setOnClickListener {
//            binding.bodyMapView.redo()
//        }
//
//        // Handle button clicks
//        binding.btnSave.setOnClickListener {
//            dialog.dismiss()
//            val bitmap = binding.bodyMapView.getBitmap()
//            val file = AppConstant.bitmapToFile(requireContext(), bitmap, "${System.currentTimeMillis()}.png")
//            Log.d("FilePath", "File saved at: ${file?.path}")
//            file?.let {
//                fileList.add(FileModel(bodyPartType, bodyPartName, it.name, it.path))
//                fileAdapter.notifyDataSetChanged()
//            } ?: run {
//                AlertUtils.showToast(requireActivity(), "Something went wrong")
//            }
//        }
//
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
//
//        val window = dialog.window
//        window?.setLayout(
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT
//        )
//        dialog.show()
    }

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

        // Data visibility
        viewModel.clientsList.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                val spinnerList = ArrayList<String>()
                for (client in data) {
                    spinnerList.add(client.clientName)
                }
                // remove duplicates
                val uniqueSpinnerList = spinnerList.distinct()
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    uniqueSpinnerList
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.allClientNameSpinner.adapter = adapter

                binding.allClientNameSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        @SuppressLint("SetTextI18n")
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
//                            binding.clientName.text = adapter.getItem(position).toString()
//                            clientId = data[position].clientId
//                            visitDetailsId = -1
//                            binding.visitName.text = ""
//                            viewModel.getFilterVisits(clientId)
                            binding.clientName.text = adapter.getItem(position).toString()

                            // This might be incorrect if 'data' and 'uniqueSpinnerList' lengths differ
                            val selectedName = adapter.getItem(position)
                            val selectedClient = data.find { it.clientName == selectedName }
                            clientId = selectedClient?.clientId ?: -1

                            visitDetailsId = -1
                            binding.visitName.text = ""
                            viewModel.getFilterVisits(clientId)
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
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
                binding.allVisitTimeSpinner.adapter = adapter

                binding.allVisitTimeSpinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        @SuppressLint("SetTextI18n")
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View,
                            position: Int,
                            id: Long
                        ) {
                            binding.visitName.text = adapter.getItem(position).toString()
                            visitDetailsId = data[position].visitDetailsId
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }
                    }
            } else {
                visitDetailsId = -1
                binding.visitName.text = ""
            }
        }
    }
}