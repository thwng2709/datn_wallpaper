package com.itsthwng.twallpaper.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ConnectionLiveDataJava (context: Context) : LiveData<Boolean>() {
    lateinit var networkCallback: ConnectivityManager.NetworkCallback
    lateinit var cm: ConnectivityManager
    var validNetworks: HashSet<Network> = HashSet()
    var executor: ExecutorService = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())

    var NETWORK_STATUS_POST_DELAY: Long = 1000L

    init {
        cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun checkValidNetworks() {
        val runnable = Runnable {
            postValue(validNetworks.size > 0)
        }
        this.schedule(runnable)
    }

    fun schedule(runable: Runnable) {
        handler.postDelayed(runable, NETWORK_STATUS_POST_DELAY)
    }

    fun cancelSchedule(runable: Runnable) {
        handler.removeCallbacksAndMessages(runable)
    }

    @Override
    override fun onActive() {
        networkCallback = createNetworkCallback()
        var networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    @Override
    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    fun createNetworkCallback(): ConnectivityManager.NetworkCallback {
        val networkCallback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        object: ConnectivityManager.NetworkCallback(){
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                var capabilities: NetworkCapabilities? = cm.getNetworkCapabilities(network)
                var hasInternetCapability : Boolean =
                    capabilities?.hasCapability (NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false;
                if (hasInternetCapability) {
                    // check if this network actually has internet
                    executor.execute {
                        var hasInternet : Boolean = DoesNetworkHaveInternet.execute(network.getSocketFactory())
                        if (hasInternet) {
                            validNetworks.add(network);
                            checkValidNetworks();
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                validNetworks.remove(network)
                checkValidNetworks()
            }
        }
        return networkCallback
    }
}