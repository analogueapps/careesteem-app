package com.aits.careesteem.view.auth.view

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.aits.careesteem.R
import com.aits.careesteem.databinding.FragmentVerifyOtpBinding
import com.aits.careesteem.databinding.FragmentWelcomeBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.AppConstant
import com.aits.careesteem.utils.ProgressLoader
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.auth.model.UserData
import com.aits.careesteem.view.auth.viewmodel.VerifyOtpViewModel
import com.aits.careesteem.view.auth.viewmodel.WelcomeViewModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VerifyOtpFragment : Fragment() {
    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!
    private val args: VerifyOtpFragmentArgs by navArgs()
    private val viewModel: VerifyOtpViewModel by viewModels()

    @Inject
    lateinit var editor: SharedPreferences.Editor

    private var userData: UserData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gson = Gson()
        userData = gson.fromJson(args.response, UserData::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        binding.tvMaskedNumber.text = AppConstant.maskPhoneNumber(userData?.contact_number ?: "1234567890")
        setupViewmodel()
        setupWidgets()
        return binding.root
    }

    private fun setupWidgets() {
        val fullText = "By entering OTP you agree to accept our Terms & Condition and Privacy Policy"
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
                    Toast.makeText(widget.context, "Terms & Condition clicked!", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(widget.context, "Privacy Policy clicked!", Toast.LENGTH_SHORT).show()
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

        // Observe OTP validation success
        viewModel.isOtpValid.observe(viewLifecycleOwner, Observer { isValid ->
            if (isValid) {
                editor.putBoolean(SharedPrefConstant.IS_LOGGED, true)
                editor.apply()
                ProgressLoader.showProgress(requireActivity())
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    ProgressLoader.dismissProgress()
                    viewLifecycleOwner.lifecycleScope.launch {
                        findNavController().navigate(R.id.preloaderFragment)
                    }
                }
            } else {
                val errorMessage = viewModel.otpError.value
                errorMessage?.let {
                    AlertUtils.showToast(requireActivity(), it)
                }
            }
        })
    }
}