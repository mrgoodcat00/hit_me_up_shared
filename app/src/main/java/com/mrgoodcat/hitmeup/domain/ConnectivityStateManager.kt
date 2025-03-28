package com.mrgoodcat.hitmeup.domain

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.mrgoodcat.hitmeup.domain.Status.Available
import com.mrgoodcat.hitmeup.domain.Status.Losing
import com.mrgoodcat.hitmeup.domain.Status.Lost
import com.mrgoodcat.hitmeup.domain.Status.Unavailable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ConnectivityStateManager @Inject constructor(ctx: Context) {

    private var connectivityManager: ConnectivityManager =
        ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun isOnline(): Boolean {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun observeNetworkStatus(): Flow<Status> {
        return callbackFlow {
            val listener = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { trySend(Available) }
                }

                override fun onLosing(network: Network, maxMsToLive: Int) {
                    super.onLosing(network, maxMsToLive)
                    launch { trySend(Losing) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { trySend(Lost) }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    launch { trySend(Unavailable) }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(listener)
            }

            awaitClose {
                Timber.d("observeNetworkStatus awaitClose")
                connectivityManager.unregisterNetworkCallback(listener)
            }
        }.distinctUntilChanged()
    }
}

enum class Status {
    Available, Unavailable, Losing, Lost
}