package com.aits.careesteem.view.visits.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVisitNotesBinding
import com.aits.careesteem.databinding.FragmentVisitsBinding
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VisitNotesFragment : Fragment() {
    private var _binding: FragmentVisitNotesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVisitNotesBinding.inflate(inflater, container, false)
        setupWidget()
        return binding.root
    }

    private fun setupWidget() {
        // Load GIF from drawable
        Glide.with(this)
            .asGif()
            .load(R.drawable.no_notes) // Replace with your GIF resource
            .into(binding.gifImageView)
    }


}