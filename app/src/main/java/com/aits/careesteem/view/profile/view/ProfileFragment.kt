package com.aits.careesteem.view.profile.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.databinding.FragmentNotificationsBinding
import com.aits.careesteem.databinding.FragmentProfileBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.getAppVersion
import com.aits.careesteem.view.auth.view.AuthActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setupWidgets()
        setupSwipeRefresh()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setupWidgets() {
        binding.appVersion.text = "Version " + getAppVersion(requireContext())

        binding.btnLogout.setOnClickListener {
            showLogout()
        }
    }

    private fun showLogout() {
        val dialog = Dialog(requireContext())
        val binding: DialogLogoutBinding =
            DialogLogoutBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        // Handle button clicks
        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            editor.clear()
            editor.apply()
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}