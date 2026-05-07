package com.itsthwng.twallpaper.di

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SessionManager @Inject constructor() {
    // tạo seed 1 lần khi process khởi động
    private val _seed = MutableStateFlow(Random.nextLong())
    val seed: StateFlow<Long> = _seed

    // nếu cần làm “làm mới session” (VD sau khi user nhấn Refresh):
    fun refreshSession() {
        _seed.value = Random.nextLong()
    }
}