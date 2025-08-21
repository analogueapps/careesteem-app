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
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.DialogLogoutBinding
import com.aits.careesteem.databinding.DialogOfflineConfirmBinding
import com.aits.careesteem.databinding.FragmentProfileBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SafeCoroutineScope
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.utils.getAppVersion
import com.aits.careesteem.view.auth.view.AuthActivity
import com.aits.careesteem.view.offline.view.OfflineActivity
import com.aits.careesteem.view.profile.model.UserDetailsResponse
import com.aits.careesteem.view.profile.viewmodel.ProfileViewModel
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
        binding.switchNotification.isChecked =
            sharedPreferences.getBoolean(SharedPrefConstant.NOTIFICATION_ENABLE, false)

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean(SharedPrefConstant.NOTIFICATION_ENABLE, true)
                editor.apply()
                AlertUtils.showToast(activity, "Notifications are enabled", ToastyType.SUCCESS)
            } else {
                editor.putBoolean(SharedPrefConstant.NOTIFICATION_ENABLE, false)
                editor.apply()
                AlertUtils.showToast(activity, "Notifications are disabled", ToastyType.SUCCESS)
            }
        }

        binding.switchFaceId.isChecked =
            sharedPreferences.getBoolean(SharedPrefConstant.LOCK_ENABLE, false)

        binding.switchFaceId.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean(SharedPrefConstant.LOCK_ENABLE, true)
                editor.apply()
                AlertUtils.showToast(activity, "Face ID are enabled", ToastyType.SUCCESS)
            } else {
                editor.putBoolean(SharedPrefConstant.LOCK_ENABLE, false)
                editor.apply()
                AlertUtils.showToast(activity, "Face ID are disabled", ToastyType.SUCCESS)
            }
        }

        binding.switchOfflineMode.isChecked =
            sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)

        binding.switchOfflineMode.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                editor.putBoolean(SharedPrefConstant.WORK_ON_OFFLINE, true)
//                editor.apply()
//                AlertUtils.showToast(activity, "Now you work with your visits offline", ToastyType.SUCCESS)
//            } else {
//                editor.putBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)
//                editor.apply()
//                AlertUtils.showToast(activity, "Now you work with your visits online", ToastyType.SUCCESS)
//            }

            // Temporarily remove listener to avoid recursion
            binding.switchOfflineMode.setOnCheckedChangeListener(null)

            // Revert the switch until user confirms
            binding.switchOfflineMode.isChecked = !isChecked

            // move to offline screen
            val intent = Intent(requireActivity(), OfflineActivity::class.java)
            startActivity(intent)
//
//            // Create and show custom confirmation dialog
//            val dialog = Dialog(requireContext()).apply {
//                val dialogBinding = DialogOfflineConfirmBinding.inflate(layoutInflater)
//                setContentView(dialogBinding.root)
//                setCancelable(false)
//
//                // Set dialog title and message
//                dialogBinding.tvTitle.text = "Confirm Mode Change"
//                dialogBinding.tvMessage.text = if (isChecked) {
//                    "Are you sure you want to switch to offline mode? Please make sure your data is downloaded before continuing."
//                } else {
//                    "Are you sure you want to switch to online mode? Your offline data will be synced."
//                }
//
//                // Positive button action
//                dialogBinding.btnPositive.setOnClickListener {
//                    dismiss()
//                    // Apply the new mode
//                    binding.switchOfflineMode.isChecked = isChecked
//                    editor.putBoolean(SharedPrefConstant.WORK_ON_OFFLINE, isChecked)
//                    editor.apply()
//
//                    val toastMessage = if (isChecked) {
//                        "Now you work with your visits offline"
//                    } else {
//                        "Now you work with your visits online"
//                    }
//                    AlertUtils.showToast(activity, toastMessage, ToastyType.SUCCESS)
//
//                    // Reattach the listener
//                    binding.switchOfflineMode.setOnCheckedChangeListener(this@ProfileFragment::onCheckedChange)
//                }
//
//                // Negative button action
//                dialogBinding.btnNegative.setOnClickListener {
//                    dismiss()
//                    // Keep switch in original (reverted) state
//                    binding.switchOfflineMode.isChecked = !isChecked
//                    // Reattach the listener
//                    binding.switchOfflineMode.setOnCheckedChangeListener(this@ProfileFragment::onCheckedChange)
//                }
//
//                // Window styling
//                window?.setBackgroundDrawableResource(android.R.color.transparent)
//                window?.setLayout(
//                    WindowManager.LayoutParams.MATCH_PARENT,
//                    WindowManager.LayoutParams.WRAP_CONTENT
//                )
//                window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//                window?.setDimAmount(0.8f)
//            }
//            dialog.show()
        }

        binding.appVersion.text = "Version " + getAppVersion(requireContext())

        binding.btnLogout.setOnClickListener {
            showLogout()
        }

        binding.btnSwitch.setOnClickListener {
            showSwitch()
        }
    }

    private fun onCheckedChange(buttonView: CompoundButton?, isChecked: Boolean) {

    }

    @SuppressLint("SetTextI18n")
    private fun showSwitch() {
        val dialog = Dialog(requireContext())
        val binding: DialogForceCheckBinding =
            DialogForceCheckBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        binding.imgPopup.setImageResource(R.drawable.switch_account)
        binding.dialogTitle.text = "You are about to switch accounts."
        binding.dialogBody.text =
            "Unsaved changes or data in your current session may be lost." +
                    "\n" +
                    "Do you want to continue?"
        binding.btnPositive.text = "Switch Account"
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
        binding.profileName.text = AppConstant.checkNull(data.name)
        binding.profileAgency.text = AppConstant.checkNull(data.Agency)
        binding.profileAge.text = AppConstant.checkAge(data.age)
        binding.profileEmail.text = AppConstant.checkNull(data.email)
        binding.profileContactNumber.text = AppConstant.checkNull(data.contact_number)
        binding.profileAddress.text = AppConstant.checkNull(data.address)
        binding.profileCity.text = AppConstant.checkNull(data.city)
        binding.profilePostCode.text = AppConstant.checkNull(data.postcode)

        if (data.profile_image_url.isNotEmpty()) {
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

//        val savedPhoto = sharedPreferences.getString(SharedPrefConstant.PROFILE_IMAGE, null)
//        if (!savedPhoto.isNullOrEmpty()) {
////            AppConstant.base64ToBitmap(savedPhoto)?.let { bitmap ->
////                val roundedDrawable = RoundedBitmapDrawableFactory.create(resources, bitmap).apply {
////                    isCircular = true
////                }
////                binding.profileImage.setImageDrawable(roundedDrawable)
////            }
//            Glide.with(requireContext())
//                .load(savedPhoto)
//                .override(400, 300)
//                
//                .error(R.drawable.logo_preview)
//                .into(binding.profileImage)
//        } else {
//            val initials = GooglePlaceHolder().getInitialsSingle(data.name)
//            val initialsBitmap = GooglePlaceHolder().createInitialsAvatar(requireContext(), initials)
//            binding.profileImage.setImageBitmap(initialsBitmap)
//        }
    }

}