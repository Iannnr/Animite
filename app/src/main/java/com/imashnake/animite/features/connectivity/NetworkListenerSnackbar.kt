package com.imashnake.animite.features.connectivity

import android.net.ConnectivityManager
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.getSystemService
import com.imashnake.animite.R

@Composable
fun NetworkListenerSnackbar() {
    val connectivityManager = LocalContext.current.getSystemService<ConnectivityManager>()
    val scope = rememberCoroutineScope()
    val connectivityListener = remember { ConnectivityListener(scope, connectivityManager) }

    val snackBarHostState = remember { SnackbarHostState() }
    SnackbarHost(hostState = snackBarHostState)

    val snackBarText = LocalContext.current.getString(R.string.connectivity_lost)

    val offline by connectivityListener.networkListener.collectAsState()

    if (offline) {
        LaunchedEffect(Unit) {
            snackBarHostState.showSnackbar(snackBarText)
        }
    }
}