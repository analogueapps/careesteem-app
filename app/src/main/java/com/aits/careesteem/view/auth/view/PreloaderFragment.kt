package com.aits.careesteem.view.auth.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentPreloaderBinding
import com.aits.careesteem.view.home.view.HomeActivity
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PreloaderFragment : Fragment() {
    private var _binding: FragmentPreloaderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPreloaderBinding.inflate(inflater, container, false)
        setupPreloader()
        return binding.root
    }

    private fun setupPreloader() {
        // Load GIF from drawable
        Glide.with(this)
            .asGif()
            .load(R.drawable.preloader_logo) // Replace with your GIF resource
            .into(binding.gifImageView)

        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            val intent = Intent(requireActivity(), HomeActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }
}