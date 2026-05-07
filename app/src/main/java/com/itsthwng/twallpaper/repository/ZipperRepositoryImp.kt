package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.dao.ZipperDao
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ZipperRepositoryImp @Inject constructor(
    private val zipperDao: ZipperDao
): ZipperRepository {

    override suspend fun getZippers(): List<ZipperImageEntity> {
        return try {
            withContext(Dispatchers.IO) { // ✅ Sử dụng IO dispatcher
                zipperDao.getAllZippers()
            }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZippers: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getZipperById(id: Int): ZipperImageEntity? {
        return try {
            val allZippers = zipperDao.getAllZippers()
            allZippers.find { it.id == id }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZipperById: ${e.message}")
            null
        }
    }

    override suspend fun getZippersByCategory(categoryId: String): List<ZipperImageEntity> {
        return try {
            val allZippers = zipperDao.getAllZippers()
            allZippers.filter { it.categoryId == categoryId }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZippersByCategory: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getZippersByAccessType(accessType: Int): List<ZipperImageEntity> {
        return try {
            val allZippers = zipperDao.getAllZippers()
            allZippers.filter { it.accessType == accessType }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZippersByAccessType: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getZippersByType(type: String): List<ZipperImageEntity> {
        return try {
            withContext(Dispatchers.IO) {
                val allZippers = zipperDao.getAllZippers()
                System.out.println("=== ZIPPER REPOSITORY DEBUG ===")
                System.out.println("Total zippers in database: ${allZippers.size}")
                System.out.println("Filtering by type: $type")
                
                val filteredZippers = allZippers.filter { it.type == type }
                System.out.println("Filtered zippers count: ${filteredZippers.size}")
                
                // Log chi tiết từng zipper
                filteredZippers.forEachIndexed { index, zipper ->
                    System.out.println("Zipper $index: ID=${zipper.id}, Type=${zipper.type}, Content=${zipper.content}")
                }
                
                filteredZippers
            }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZippersByType: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getZippersByTypeAndAccessType(type: String, accessType: Int): List<ZipperImageEntity> {
        return try {
            withContext(Dispatchers.IO) {
                val allZippers = zipperDao.getAllZippers()
                allZippers.filter { it.type == type && it.accessType == accessType }
            }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "getZippersByTypeAndAccessType: ${e.message}")
            emptyList()
        }
    }

    override suspend fun saveZipper(zipper: ZipperImageEntity) {
        try {
            zipperDao.upsertZipper(zipper)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "saveZipper: ${e.message}")
        }
    }

    override suspend fun saveZippers(zippers: List<ZipperImageEntity>) {
        try {
            zipperDao.upsertZippers(zippers)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "saveZippers: ${e.message}")
        }
    }

    override suspend fun deleteZipperById(id: String) {
        try {
            zipperDao.deleteZipperById(id)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "deleteZipperById: ${e.message}")
        }
    }

    override suspend fun deleteAllZippers() {
        try {
            zipperDao.deleteAllZippers()
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "deleteAllZippers: ${e.message}")
        }
    }

    override fun observeZippers(): Flow<List<ZipperImageEntity>> = flow {
        try {
            val zippers = zipperDao.getAllZippers()
            emit(zippers)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "observeZippers: ${e.message}")
            emit(emptyList())
        }
    }

    override fun observeZippersByCategory(categoryId: String): Flow<List<ZipperImageEntity>> = flow {
        try {
            val allZippers = zipperDao.getAllZippers()
            val filteredZippers = allZippers.filter { it.categoryId == categoryId }
            emit(filteredZippers)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "observeZippersByCategory: ${e.message}")
            emit(emptyList())
        }
    }

    override fun observeZippersByType(type: String): Flow<List<ZipperImageEntity>> = flow {
        try {
            val allZippers = zipperDao.getAllZippers()
            val filteredZippers = allZippers.filter { it.type == type }
            emit(filteredZippers)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "observeZippersByType: ${e.message}")
            emit(emptyList())
        }
    }

    override fun observeZippersByTypeAndAccessType(type: String, accessType: Int): Flow<List<ZipperImageEntity>> = flow {
        try {
            val allZippers = zipperDao.getAllZippers()
            val filteredZippers = allZippers.filter { it.type == type && it.accessType == accessType }
            emit(filteredZippers)
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "observeZippersByTypeAndAccessType: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun updateZipperAccessType(zipperId: Int) {
        try {
            val zipper = zipperDao.getZipperById(zipperId)
            if(zipper != null){
                val newAccessType = Constants.IMAGE_TYPE_FREE
                val updatedZipper = zipper.copy(accessType = newAccessType)
                zipperDao.upsertZipper(updatedZipper)
            }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "updateZipperAccessType: ${e.message}")
        }
    }

    override suspend fun updateSelectedZipper(zipper: ZipperImageEntity, type: Int) {
        try {
            // Ensure zipper exists in database before updating history
            zipperDao.upsertZipper(zipper)

            // Update history status based on type
            // Type constants should match PreviewActivity: HOME = 1, LOCK = 2, BOTH = 3
            when(type) {
                1 -> { // HOME
                    zipperDao.applyHomeSelection(zipper.id)
                }
                2 -> { // LOCK
                    zipperDao.applyLockSelection(zipper.id)
                }
                3 -> { // BOTH
                    zipperDao.applyBothSelection(zipper.id, zipper.id)
                }
                else -> {}
            }
        } catch (e: Exception) {
            Logger.e("ZipperRepository", "updateSelectedZipper: ${e.message}")
        }
    }
}
