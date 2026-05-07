package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import kotlinx.coroutines.flow.Flow

interface ZipperRepository {
    suspend fun getZippers(): List<ZipperImageEntity>
    suspend fun getZipperById(id: Int): ZipperImageEntity?
    suspend fun getZippersByCategory(categoryId: String): List<ZipperImageEntity>
    suspend fun getZippersByAccessType(accessType: Int): List<ZipperImageEntity>
    suspend fun getZippersByType(type: String): List<ZipperImageEntity>
    suspend fun getZippersByTypeAndAccessType(type: String, accessType: Int): List<ZipperImageEntity>
    suspend fun saveZipper(zipper: ZipperImageEntity)
    suspend fun saveZippers(zippers: List<ZipperImageEntity>)
    suspend fun deleteZipperById(id: String)
    suspend fun deleteAllZippers()
    fun observeZippers(): Flow<List<ZipperImageEntity>>
    fun observeZippersByCategory(categoryId: String): Flow<List<ZipperImageEntity>>
    fun observeZippersByType(type: String): Flow<List<ZipperImageEntity>>
    fun observeZippersByTypeAndAccessType(type: String, accessType: Int): Flow<List<ZipperImageEntity>>
    suspend fun updateZipperAccessType(zipperId: Int)
    // History tracking methods
    suspend fun updateSelectedZipper(zipper: ZipperImageEntity, type: Int)
}
