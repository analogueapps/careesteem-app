package com.aits.careesteem.view.webview.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aits.careesteem.R
import com.aits.careesteem.databinding.ActivityWebViewBinding
import java.net.URLEncoder

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
            // Additional settings for better PDF handling
            setSupportZoom(true)
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        fileUrl?.let { url ->
            if (url.contains(".pdf", ignoreCase = true)) {
                // For AWS S3 URLs with authentication parameters
                if (url.contains("X-Amz-Signature")) {
                    // Option 1: Use Google Docs viewer (works for public files)
                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                    val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$encodedUrl"
                    binding.webView.loadUrl(googleDocsUrl)

                    // Option 2: Download and display locally (better for authenticated URLs)
                    // downloadAndDisplayPdf(url)
                } else {
                    // Standard PDF handling
                    binding.webView.loadUrl("https://docs.google.com/gview?embedded=true&url=$url")
                }
            } else {
                binding.webView.loadUrl(url)
            }
        }
    }
}