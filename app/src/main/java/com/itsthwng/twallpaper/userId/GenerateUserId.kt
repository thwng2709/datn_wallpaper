package com.itsthwng.twallpaper.userId

import java.security.SecureRandom
import android.util.Base64

object GenerateUserId {
    private val rng = SecureRandom()
    private var lastMs: Long = 0L
    private var seq: Int = 0  // 0..65535

    @Synchronized
    fun generate(): String {
        val now = System.currentTimeMillis()

        // sequence chống đụng trong cùng 1 ms
        if (now == lastMs) {
            seq = (seq + 1) and 0xFFFF
        } else {
            lastMs = now
            seq = 0
        }

        // 14 bytes: [ ts(6) | seq(2) | rand(6) ]
        val b = ByteArray(14)

        // ts: 6 byte big-endian (48-bit millis)
        var t = now
        b[0] = ((t shr 40) and 0xFF).toByte()
        b[1] = ((t shr 32) and 0xFF).toByte()
        b[2] = ((t shr 24) and 0xFF).toByte()
        b[3] = ((t shr 16) and 0xFF).toByte()
        b[4] = ((t shr 8)  and 0xFF).toByte()
        b[5] = ( t         and 0xFF).toByte()

        // seq: 2 byte big-endian
        b[6] = ((seq ushr 8) and 0xFF).toByte()
        b[7] = ( seq         and 0xFF).toByte()

        // rand: 6 byte
        val r = ByteArray(6)
        rng.nextBytes(r)
        System.arraycopy(r, 0, b, 8, 6)

        // Base64 URL-safe, no padding → ~20 kí tự
        var s = Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_WRAP)
        s = s.trimEnd('=') // đề phòng, dù NO_WRAP thường không sinh '='
        return s
    }

    /** Giải timestamp (ms) từ TSID (đã tạo bởi generate ở trên). */
    fun extractMillis(id: String): Long {
        // thêm padding nếu cần để Base64 decode được
        val padded = when (id.length % 4) {
            2 -> id + "=="
            3 -> id + "="
            else -> id
        }
        val bytes = Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP)
        require(bytes.size >= 6) { "Invalid TSID" }
        return ((bytes[0].toLong() and 0xFF) shl 40) or
                ((bytes[1].toLong() and 0xFF) shl 32) or
                ((bytes[2].toLong() and 0xFF) shl 24) or
                ((bytes[3].toLong() and 0xFF) shl 16) or
                ((bytes[4].toLong() and 0xFF) shl 8)  or
                ( bytes[5].toLong() and 0xFF)
    }
}
