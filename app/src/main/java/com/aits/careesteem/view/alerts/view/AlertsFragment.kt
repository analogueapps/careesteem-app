package com.aits.careesteem.view.alerts.view

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentAlertsBinding
import com.aits.careesteem.databinding.FragmentCheckOutBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertsFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        setupWidget()
        return binding.root
    }

    private fun setupWidget() {
        binding.btnCreateUnscheduledVisit.setOnClickListener {
            findNavController().navigate(R.id.addAlertsFragment)
        }

        binding.btnUndo.setOnClickListener {
            binding.bodyMapView.undo()
        }

        binding.btnRedo.setOnClickListener {
            binding.bodyMapView.redo()
        }
    }

}