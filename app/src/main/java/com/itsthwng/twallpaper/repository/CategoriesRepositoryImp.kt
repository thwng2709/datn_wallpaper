package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.dao.CategoryDao
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.toCategoriesItem
import com.itsthwng.twallpaper.data.model.toCategoryEntity
import com.itsthwng.twallpaper.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoriesRepositoryImp @Inject constructor(
    private val categoryDao: CategoryDao
): CategoriesRepository {
    override suspend fun getCategories(): List<SettingData.CategoriesItem> {
        try {
            val listCate = categoryDao.getAllCategories()
            return listCate.map {
                it.toCategoriesItem()
            }
        } catch (e : Exception){
            Logger.e("CategoryRepository", "getCategories: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun saveCategories(categories: List<SettingData.CategoriesItem>) {
        try {
            val listCate = categories.map { it.toCategoryEntity() }
            categoryDao.upsertCategories(listCate)
        } catch (e : Exception){
            Logger.e("CategoryRepository", "saveCategories: ${e.message}")
        }
    }

    override fun observeCategories(): Flow<List<SettingData.CategoriesItem>> =
        categoryDao.observeAll().map { list -> list.map { it.toCategoriesItem() } }
}