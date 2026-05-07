package com.itsthwng.twallpaper.extension

fun <E> List<E>?.toArrayList(): ArrayList<E> {
    val list = ArrayList<E>()
    for (data in this ?: emptyList()) {
        list.add(data)
    }
    return list
}