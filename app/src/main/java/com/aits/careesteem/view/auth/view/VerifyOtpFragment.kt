package com.aits.careesteem.view.auth.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.DialogForceCheckBinding
import com.aits.careesteem.databinding.FragmentVerifyOtpBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.SmsBroadcastReceiver
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.auth.viewmodel.VerifyOtpViewModel
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

@AndroidEntryPoint
class VerifyOtpFragment : Fragment() {
    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!
    private val args: VerifyOtpFragmentArgs by navArgs()
    private val viewModel: VerifyOtpViewModel by viewModels()

    @Inject
    lateinit var editor: SharedPreferences.Editor

    //private var userData: SendOtpUserLoginResponse.Data? = null

    private var smsBroadcastReceiver: SmsBroadcastReceiver? = null
    private val REQ_USER_CONSENT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val gson = Gson()
//        userData = gson.fromJson(args.response, SendOtpUserLoginResponse.Data::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        binding.tvMaskedNumber.text = AppConstant.maskPhoneNumber(args.mobileNo ?: "1234567890")
        setupViewmodel()
        setupWidgets()
        startSmsUserConsent()
        return binding.root
    }

    private fun startSmsUserConsent() {
        val client = SmsRetriever.getClient(requireActivity())
        //We can add sender phone number or leave it blank
        // I'm adding null here
        //We can add sender phone number or leave it blank
        // I'm adding null here
        client.startSmsUserConsent(null).addOnSuccessListener { }.addOnFailureListener {
            Toast.makeText(
                requireContext(),
                "On OnFailure",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun registerBroadcastReceiver() {
        smsBroadcastReceiver = SmsBroadcastReceiver()
        smsBroadcastReceiver!!.smsBroadcastReceiverListener =
            object : SmsBroadcastReceiver.SmsBroadcastReceiverListener {
                override fun onSuccess(intent: Intent) {
                    startActivityForResult(intent, REQ_USER_CONSENT)
                }

                override fun onFailure() {}
            }
        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.registerReceiver(
                smsBroadcastReceiver, intentFilter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            activity?.registerReceiver(smsBroadcastReceiver, intentFilter)
        }
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        activity?.unregisterReceiver(smsBroadcastReceiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_USER_CONSENT) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                //That gives all message to us.
                // We need to get the code from inside with regex
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                //                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                getOtpFromMessage(message)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getOtpFromMessage(message: String?) {
        // This will match any 6 digit number in the message
        try {
            val pattern = Pattern.compile("(|^)\\d{6}")
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                binding.etOtp.setText(matcher.group(0)) //ur otp view
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupWidgets() {
        val fullText =
            "By entering OTP you agree to accept our Terms & Condition and Privacy Policy"
        // Create a SpannableStringBuilder
        val spannableString = SpannableStringBuilder(fullText)

        // Define the start and end indices for "Terms & Condition"
        val termsStart = fullText.indexOf("Terms & Condition")
        val termsEnd = termsStart + "Terms & Condition".length

        // Define the start and end indices for "Privacy Policy"
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length

        // Apply color and click action for "Terms & Condition"
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE), // Change color
            termsStart, termsEnd,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
//                    Toast.makeText(widget.context, "Terms & Condition clicked!", Toast.LENGTH_SHORT)
//                        .show()
                    val direction =
                        VerifyOtpFragmentDirections.actionVerifyOtpFragmentToWebViewFragment2(
                            fileUrl = "https://careesteem.co.uk/terms-and-conditions"
                        )
                    findNavController().navigate(direction)
                    // Add your navigation or action here
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
//                    ds.color = Color.CYAN // Keep the text color consistent
//                    ds.isUnderlineText = false // Remove underline
//                    ds.bgColor = Color.TRANSPARENT // Remove background color
                }
            },
            termsStart, termsEnd,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply color and click action for "Privacy Policy"
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE), // Change color
            privacyStart, privacyEnd,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
//                    Toast.makeText(widget.context, "Privacy Policy clicked!", Toast.LENGTH_SHORT)
//                        .show()
                    val direction =
                        VerifyOtpFragmentDirections.actionVerifyOtpFragmentToWebViewFragment2(
                            fileUrl = "https://careesteem.co.uk/privacy-policy"
                        )
                    findNavController().navigate(direction)
                    // Add your navigation or action here
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
//                    ds.color = Color.CYAN // Keep the text color consistent
//                    ds.isUnderlineText = false // Remove underline
//                    ds.bgColor = Color.TRANSPARENT // Remove background color
                }
            },
            privacyStart, privacyEnd,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the SpannableString to the TextView
        binding.termsText.text = spannableString

        // Enable click handling
        binding.termsText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupViewmodel() {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.userMobile = args.mobileNo
        viewModel.userCountryId = args.countryId

//        viewModel.isVerifyButtonEnabled.observe(viewLifecycleOwner) { enabled ->
//            binding.btnVerifyOtp.isEnabled = enabled
//        }

        binding.onClickResendOtp.setOnClickListener {
            viewModel.onResendOtp(requireActivity())
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressLoader.showProgress(requireActivity())
            } else {
                ProgressLoader.dismissProgress()
            }
        }

        // Observe OTP validation success
        viewModel.isOtpValid.observe(viewLifecycleOwner, Observer { isValid ->
            if (isValid) {
                if (viewModel.onTermsCheck.value == true) {
                    viewModel.callVerifyOtpApi(requireActivity())
                } else {
                    AlertUtils.showToast(
                        requireActivity(),
                        "Please accept the terms and conditions",
                        ToastyType.INFO
                    )
                }
            } else {
                val errorMessage = viewModel.otpError.value
                errorMessage?.let {
                    AlertUtils.showToast(requireActivity(), it, ToastyType.ERROR)
                }
            }
        })

        viewModel.otpVerifyResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                val gson = Gson()
//                val dataString = gson.toJson(response.data[0])
                if (response.dbList.size == 1) {
                    viewModel.onAgencySelected(requireActivity(), response.dbList[0])
                } else if (response.dbList.size > 1) {
                    val dbString = gson.toJson(response.dbList)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val direction =
                            VerifyOtpFragmentDirections.actionVerifyOtpFragmentToSelectAgencyFragment(
                                response = dbString
                            )
                        findNavController().navigate(direction)
                    }
                } else {
                    //AlertUtils.showToast(requireActivity(), "No agency found. Contact admin")
                    //requireActivity().finishAffinity()
                    showNoDbList()
                }
            }
        })

        viewModel.createHashToken.observe(viewLifecycleOwner, Observer { response ->
            if (response != null) {
                val gson = Gson()
                val dataString = gson.toJson(response.data[0])
                editor.putString(SharedPrefConstant.USER_DATA, dataString)
                editor.apply()
                viewLifecycleOwner.lifecycleScope.launch {
                    val direction =
                        VerifyOtpFragmentDirections.actionVerifyOtpFragmentToSetupPasscodeFragment(
                            response = dataString
                        )
                    findNavController().navigate(direction)
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun showNoDbList() {
        if (!isAdded) return  // Fragment is not attached yet

        val dialog = Dialog(requireContext())
        val binding: DialogForceCheckBinding =
            DialogForceCheckBinding.inflate(layoutInflater)
        dialog.window?.setDimAmount(0.8f)
        dialog.setContentView(binding.root)
        dialog.setCancelable(AppConstant.FALSE)

        binding.dialogTitle.text = "No Company Found"
        binding.dialogBody.text =
            "We couldn't find an company with the provided number. Would you like to try another number or exit?"
        binding.btnPositive.text = "Try Again"
        binding.btnNegative.text = "Exit"

        // Handle button clicks
        binding.btnPositive.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.welcomeFragment)
        }
        binding.btnNegative.setOnClickListener {
            dialog.dismiss()
            requireActivity().finishAffinity()
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