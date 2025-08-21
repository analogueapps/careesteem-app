package com.aits.careesteem.view.offline.view

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityOnlineBinding
import com.aits.careesteem.utils.AlertUtils
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.utils.ToastyType
import com.aits.careesteem.view.home.view.HomeActivity
import com.aits.careesteem.view.offline.viewmodel.OnlineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class OnlineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnlineBinding

    @Inject
    lateinit var editor: SharedPreferences.Editor

    private val viewModel: OnlineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityOnlineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Force show status bar
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Extend layout to draw behind status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Make status bar transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        // Set status bar icons to dark (black)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            view.setPadding(
                0,
                insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                0,
                insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        handleViewModel()
    }

    @SuppressLint("NewApi", "SetTextI18n")
    private fun handleViewModel() {
        lifecycleScope.launch {
            viewModel.syncAll()
        }

        // Observe progress
        viewModel.progress.observe(this) { progress ->
            binding.progressBar.progress = progress
            binding.statusText.text = "Syncing... $progress%"
        }

        // Observe completion
        viewModel.isCompleted.observe(this) { completed ->
            if (completed) {
                editor.putBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)
                editor.apply()
                AlertUtils.showToast(this, "Now you work with your visits online", ToastyType.SUCCESS)
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

    }
}