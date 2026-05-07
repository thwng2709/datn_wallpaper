package com.itsthwng.twallpaper.utils

import androidx.annotation.WorkerThread
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import javax.net.SocketFactory

object DoesNetworkHaveInternet {
    private val HOST_NAME = "8.8.8.8"
    private val PORT = 53
    private val CONNECT_TIMEOUT = 1500

    @WorkerThread
    fun execute(socketFactory: SocketFactory): Boolean {
        try {
            val socket: Socket = socketFactory.createSocket()
            socket.connect(InetSocketAddress(HOST_NAME, PORT), CONNECT_TIMEOUT)
            socket.close()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }
}