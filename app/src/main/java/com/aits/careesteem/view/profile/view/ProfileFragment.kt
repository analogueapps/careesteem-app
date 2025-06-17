package com.aits.careesteem.view.profile.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.databinding.FragmentNotificationsBinding
import com.aits.careesteem.databinding.FragmentProfileBinding
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.GooglePlaceHolder
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.getAppVersion
import com.aits.careesteem.view.auth.view.AuthActivity
import com.aits.careesteem.view.clients.viewmodel.ClientsViewModel
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.profile.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
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

    // Viewmodel
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUserDetailsById(requireActivity())
    }

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
        setupViewModel()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setupWidgets() {
        binding.appVersion.text = "Version " + getAppVersion(requireContext())

        binding.btnLogout.setOnClickListener {
            showLogout()
        }

        binding.btnSwitch.setOnClickListener {
            showSwitch()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSwitch() {
        val dialog = Dialog(requireContext())
        val binding: DialogForceCheckBinding =
            DialogForceCheckBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        binding.dialogTitle.text = "Switch Account"
        binding.dialogBody.text = "If you wish to switch your account, please check your current ongoing visits and any unsaved data will be lost beforehand, as you may lose the status of your ongoing visits."
        binding.btnPositive.text = "Proceed"
        binding.btnNegative.text = "Cancel"

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

    private fun showLogout() {
        val dialog = Dialog(requireContext())
        val binding: DialogLogoutBinding =
            DialogLogoutBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
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
                    viewModel.getUserDetailsById(requireActivity())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
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

        // Data visibility
        viewModel.userDetails.observe(viewLifecycleOwner) { data ->
            if (data != null) {
                updateProfileDetails(data)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProfileDetails(data: UserDetailsResponse.Data) {
        binding.profileName.text = data.name
        binding.profileAgency.text = data.Agency
        binding.profileAge.text = if (data.age == 0) "" else data.age.toString()
        binding.profileEmail.text = data.email
        binding.profileContactNumber.text = data.contact_number
        binding.profileAddress.text = data.address
        binding.profileCity.text = data.city
        binding.profilePostCode.text = data.postcode

        if(data.profile_image_url.isNotEmpty()) {
            editor.putString(SharedPrefConstant.PROFILE_IMAGE, data.profile_image_url)
            editor.apply()
        }
        requireActivity().invalidateOptionsMenu()

//        // Convert the Base64 string to a Bitmap
//        val bitmap = AppConstant.base64ToBitmap(data.profile_photo)
//
//        // Set the Bitmap to the ImageView (if conversion was successful)
//        bitmap?.let {
//            binding.profileImage.setImageBitmap(it)
//        }

        val savedPhoto = sharedPreferences.getString(SharedPrefConstant.PROFILE_IMAGE, null)
        if (!savedPhoto.isNullOrEmpty()) {
//            AppConstant.base64ToBitmap(savedPhoto)?.let { bitmap ->
//                val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap).apply {
//                    isCircular = true
//                }
//                binding.profileImage.setImageDrawable(roundedDrawable)
//            }
            Glide.with(requireContext())
                .load(savedPhoto)
                .override(400, 300)
                .placeholder(R.drawable.logo_preview)
                .error(R.drawable.logo_preview)
                .into(binding.profileImage)
        } else {
            val initials = GooglePlaceHolder().getInitialsSingle(data.name)
            val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(requireContext(), initials)
            binding.profileImage.setImageBitmap(initialsBitmap)
        }
    }

}