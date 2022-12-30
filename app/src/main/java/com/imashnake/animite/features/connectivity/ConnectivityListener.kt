package com.imashnake.animite.features.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConnectivityListener(
    private val scope: CoroutineScope,
    private val connectivityManager: ConnectivityManager?
) {

    private val _networkLost = Channel<Boolean>()
    val networkListener = _networkLost.receiveAsFlow()
        .onStart {
            connectivityManager?.registerNetworkCallback(networkRequest, listener)
        }
        .stateIn(scope, SharingStarted.Lazily, false)

    private val networkRequest = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private val listener = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            send(true)
        }

        override fun onAvailable(network: Network) {
            send(false)
        }
    }

    private fun send(unavailable: Boolean) {
        scope.launch {
            _networkLost.send(unavailable)
        }
    }
}