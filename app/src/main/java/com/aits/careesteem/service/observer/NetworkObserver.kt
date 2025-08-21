package com.aits.careesteem.service.observer

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.aits.careesteem.service.manager.SyncManager
import com.aits.careesteem.utils.SharedPrefConstant
import com.aits.careesteem.view.offline.view.OnlineActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncManager: SyncManager,
    private val sharedPreferences: SharedPreferences
) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun register() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    scope.launch {
                        delay(3000) // wait a bit for DNS + network to stabilize
                        //syncManager.startSync()
                        if(sharedPreferences.getBoolean(SharedPrefConstant.WORK_ON_OFFLINE, false)) {
                            val intent = Intent(context, OnlineActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            }
                            context.startActivity(intent)
                        }
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    // Internet lost
                }
            }
        )
    }
}
