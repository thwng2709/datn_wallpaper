package com.itsthwng.twallpaper.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class Network @Inject constructor(
    @ApplicationContext context: Context
) : NetworkConnectivity {
    private val context = context
    override fun getNetworkInfo(): NetworkInfo? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo
    }

    override fun isConnected(): Boolean {
        val info = getNetworkInfo()
        return info != null && info.isConnected
    }

    override fun getDownSpeed(): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)
        return nc?.linkDownstreamBandwidthKbps ?: 1000
    }
}

interface NetworkConnectivity {
    fun getNetworkInfo(): NetworkInfo?
    fun isConnected(): Boolean
    fun getDownSpeed(): Int
}