package com.aits.careesteem.view.webview.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityWebViewBinding.inflate(layoutInflater)
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

        val fileUrl = intent.getStringExtra("fileUrl")

        setupUi(fileUrl)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupUi(fileUrl: String?) {
        binding.closeButton.setOnClickListener {
            finish()
        }

        binding.webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            domStorageEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        fileUrl?.let {
            if (it.endsWith(".pdf", ignoreCase = true)) {
                // Use Google Docs viewer for PDFs
                val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=${it}"
                binding.webView.loadUrl(googleDocsUrl)
            } else {
                // Load image or other content directly
                binding.webView.loadUrl(it)
            }
        }
    }
}