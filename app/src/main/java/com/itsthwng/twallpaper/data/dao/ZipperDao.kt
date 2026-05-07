package com.itsthwng.twallpaper.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.itsthwng.twallpaper.data.entity.ZipperImageEntity
import com.itsthwng.twallpaper.data.model.LocalFlags
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ZipperDao {
    @Upsert
    abstract suspend fun upsertZipper(zipper: ZipperImageEntity)

    @Upsert
    abstract suspend fun upsertZippers(zipper: List<ZipperImageEntity>)

    @Query("DELETE FROM zipper_image_entity WHERE zipperId = :id")
    abstract suspend fun deleteZipperById(id: String)

    @Query("DELETE FROM zipper_image_entity")
    abstract fun deleteAllZippers()

    @Query("SELECT * FROM zipper_image_entity")
    abstract suspend fun getAllZippers(): List<ZipperImageEntity>

    @Query("SELECT * FROM zipper_image_entity WHERE zipperId = :id")
    abstract suspend fun getZipperById(id: Int): ZipperImageEntity?

    @Query("""
        SELECT zipperId AS wallpaperId, isSelectedHome, isSelectedLock, accessType, 0 AS isFavorite, 0 AS isDownloaded, 0 AS isForYou
        FROM zipper_image_entity
        WHERE zipperId IN (:ids)
    """)
    abstract suspend fun getLocalFlags(ids: List<Int>): List<LocalFlags>

    // History tracking methods
    @Query("""
        UPDATE zipper_image_entity
        SET isSelectedLock = CASE
            WHEN zipperId = :id THEN 1
            WHEN isSelectedLock = 1 THEN 3
            ELSE isSelectedLock
        END
    """)
    abstract suspend fun applyLockSelection(id: Int): Int

    @Query("""
        UPDATE zipper_image_entity
        SET isSelectedHome = CASE
            WHEN zipperId = :id THEN 1
            WHEN isSelectedHome = 1 THEN 3
            ELSE isSelectedHome
        END
    """)
    abstract suspend fun applyHomeSelection(id: Int): Int

    @Query("""
        UPDATE zipper_image_entity
        SET
            isSelectedLock = CASE
                WHEN zipperId = :lockId THEN 1
                WHEN isSelectedLock = 1 THEN 3
                ELSE isSelectedLock
            END,
            isSelectedHome = CASE
                WHEN zipperId = :homeId THEN 1
                WHEN isSelectedHome = 1 THEN 3
                ELSE isSelectedHome
            END
        WHERE zipperId IN (:lockId, :homeId)
           OR isSelectedLock = 1
           OR isSelectedHome = 1
    """)
    abstract suspend fun applyBothSelection(lockId: Int, homeId: Int): Int

    @Query("""
        SELECT * FROM zipper_image_entity
        WHERE isSelectedLock = 3 OR isSelectedHome = 3
    """)
    abstract suspend fun getHistoryAll(): List<ZipperImageEntity>

    @Query("""
        SELECT * FROM zipper_image_entity
        WHERE isSelectedLock IN (1, 3) OR isSelectedHome IN (1, 3)
        ORDER BY zipperId DESC
    """)
    abstract fun observeHistoryZippers(): Flow<List<ZipperImageEntity>>

    @Query("""
        SELECT * FROM zipper_image_entity
        WHERE isSelectedLock = 1 OR isSelectedHome = 1
    """)
    abstract fun observeCurrentZippers(): Flow<List<ZipperImageEntity>>

    @Query("""
        UPDATE zipper_image_entity
        SET isSelectedLock = CASE WHEN isSelectedLock = 3 THEN 0 ELSE isSelectedLock END,
            isSelectedHome = CASE WHEN isSelectedHome = 3 THEN 0 ELSE isSelectedHome END
        WHERE zipperId = :id
    """)
    abstract suspend fun removeFromHistory(id: Int)
}