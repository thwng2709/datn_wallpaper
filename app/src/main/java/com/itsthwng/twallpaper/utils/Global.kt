package com.itsthwng.twallpaper.utils

import android.text.TextUtils
import java.text.DecimalFormat

object Global {




    @JvmStatic
    fun prettyCount(number: Number): String {
        return try {
            val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
            val numValue = number.toLong()
            val value = Math.floor(Math.log10(numValue.toDouble())).toInt()
            val base = value / 3
            if (value >= 3 && base < suffix.size) {
                val value2 = numValue / Math.pow(10.0, (base * 3).toDouble())
                if (value2.toString().contains(".")) {
                    val num =
                        value2.toString().split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()[value2.toString().split("\\.".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray().size - 1]
                    if (num.contains("0")) {
                        DecimalFormat("#0").format(value2) + suffix[base]
                    } else {
                        DecimalFormat("#0.0").format(value2) + suffix[base]
                    }
                } else {
                    DecimalFormat("#0").format(value2) + suffix[base]
                }
            } else {
                DecimalFormat("#,##0").format(numValue)
            }
        } catch (e: Exception) {
            number.toString()
        }
    }


    fun listOfIntegerToString(list: List<Int>): String? {
        return TextUtils.join(",", list)
    }

    fun convertStringToLis(s: String): List<Int> {


        if (s.isEmpty()) {
            return listOf()
        }
        val stringList = s.split(",")

        // Convert each string in the list to an Int.
        return stringList.map { it.toInt() }


    }
}