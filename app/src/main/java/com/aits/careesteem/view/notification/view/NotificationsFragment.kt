package com.aits.careesteem.view.notification.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentNotificationsBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.notification.adapter.NotificationAdapter
import com.aits.careesteem.view.notification.model.NotificationListResponse
import com.aits.careesteem.view.notification.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsFragment : Fragment(), NotificationAdapter.OnDeleteItemItemClick {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    // Viewmodel
    private val viewModel: NotificationViewModel by activityViewModels()

    // Adapter
    private lateinit var notificationAdapter: NotificationAdapter

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        viewModel.getNotificationList(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        setupToolbar()
        setupAdapter()
        setupSwipeRefresh()
        setupViewModel()
        return binding.root
    }

    private fun setupAdapter() {
        notificationAdapter = NotificationAdapter(requireContext(), this@NotificationsFragment)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = notificationAdapter
    }

    private fun setupSwipeRefresh() {
        val coroutineScope = SafeCoroutineScope(SupervisorJob() + Dispatchers.Main)
        binding.swipeRefresh.setOnRefreshListener {
            coroutineScope.launch {
                try {
                    delay(2000)
                    binding.swipeRefresh.isRefreshing = AppConstant.FALSE
                    viewModel.getNotificationList(requireActivity())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setupViewModel() {
        binding.tvClearAll.setOnClickListener {
            showClearAllPopup()
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        // Data visibility
        viewModel.notificationList.observe(viewLifecycleOwner) { data ->
            updateNotificationCount(data)
            if (data.isNotEmpty()) {
                binding.apply {
                    emptyLayout.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    tvClearAll.visibility = View.VISIBLE
                }
                notificationAdapter.updateList(data)
            } else {
                binding.apply {
                    emptyLayout.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    tvClearAll.visibility = View.GONE
                }
            }
        }
    }

    private fun updateNotificationCount(list: List<NotificationListResponse.Data>) {
        editor.putString(SharedPrefConstant.NOTIFICATION_COUNT, list.size.toString()).apply()
        //editor.putString(SharedPrefConstant.NOTIFICATION_COUNT, "10").apply()
        requireActivity().invalidateOptionsMenu()
    }

    @SuppressLint("SetTextI18n")
    private fun showClearAllPopup() {
        if (!isAdded) return

        val dialog = Dialog(requireContext()).apply {
            val binding = DialogForceCheckBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCancelable(false)

            binding.imgPopup.setImageResource(R.drawable.sending_alerts)
            binding.dialogTitle.text = "Clear Notifications"
            binding.dialogBody.text = "Are you sure you want to clear all notifications?"

            binding.btnPositive.setOnClickListener {
                dismiss()
                viewModel.clearAllNotification(requireActivity())
            }

            binding.btnNegative.setOnClickListener { dismiss() }

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

    private fun setupToolbar() {
        (activity as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(AppConstant.FALSE)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDeleteItemItemClicked(data: NotificationListResponse.Data) {
        if (!isAdded) return

        val dialog = Dialog(requireContext()).apply {
            val binding = DialogForceCheckBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setCancelable(false)

            binding.imgPopup.setImageResource(R.drawable.sending_alerts)
            binding.dialogTitle.text = "Clear Notification"
            binding.dialogBody.text = "Are you sure you want to clear this notification?"

            binding.btnPositive.setOnClickListener {
                dismiss()
                viewModel.clearSingleNotification(requireActivity(), data)
            }

            binding.btnNegative.setOnClickListener { dismiss() }

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