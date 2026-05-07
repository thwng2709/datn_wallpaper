package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.model.SettingData
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    suspend fun getCategories(): List<SettingData.CategoriesItem>
    suspend fun saveCategories(categories: List<SettingData.CategoriesItem>)

    fun observeCategories(): Flow<List<SettingData.CategoriesItem>>
}