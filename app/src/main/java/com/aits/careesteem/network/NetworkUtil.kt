package com.aits.careesteem.network

import android.content.Context
import android.net.ConnectivityManager
import com.aits.careesteem.utils.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NetworkManager @Inject constructor(@ApplicationContext private val context: Context) {
    fun isConnected(): Boolean {
        return NetworkUtils.isNetworkAvailable(context)
    }
}
