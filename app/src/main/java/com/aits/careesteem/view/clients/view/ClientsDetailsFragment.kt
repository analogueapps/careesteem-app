package com.aits.careesteem.view.clients.view

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogUnscheduledVisitBinding
import com.aits.careesteem.databinding.FragmentClientsBinding
import com.aits.careesteem.databinding.FragmentClientsDetailsBinding
import com.aits.careesteem.utils.AppConstant
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ClientsDetailsFragment : Fragment() {
    private var _binding: FragmentClientsDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientsDetailsBinding.inflate(inflater, container, false)
        setupWidget()
        return binding.root

    }

    private fun setupWidget() {
        binding.btnCreateUnscheduledVisit.setOnClickListener {
            val dialog = Dialog(requireContext())
            val binding: DialogUnscheduledVisitBinding =
                DialogUnscheduledVisitBinding.inflate(layoutInflater)

            dialog.setContentView(binding.root)
            dialog.setCancelable(AppConstant.FALSE)

            // Handle button clicks
            binding.btnPositive.setOnClickListener {
                dialog.dismiss()
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
    }
}