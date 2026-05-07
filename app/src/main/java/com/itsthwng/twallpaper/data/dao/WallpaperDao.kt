package com.itsthwng.twallpaper.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.itsthwng.twallpaper.data.entity.WallpaperEntity
import com.itsthwng.twallpaper.data.model.LocalFlags
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WallpaperDao {

    @Insert
    abstract fun insertWallpaper(listWallpaper: List<WallpaperEntity>)

    @Query("SELECT * FROM wallpapers_entity")
    abstract fun getAllWallpapers(): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers_entity WHERE wallpaperId = :id")
    abstract fun getWallpaperById(id: Int): WallpaperEntity?

    @Query("SELECT * FROM wallpapers_entity WHERE category_id = :id")
    abstract fun getWallpapersByCategoryId(id: String): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers_entity WHERE is_favorite = 1")
    abstract fun getFavoriteWallpapers(): List<WallpaperEntity>

    @Query("SELECT * FROM wallpapers_entity WHERE is_downloaded > 0 ORDER BY is_downloaded DESC")
    abstract fun getDownloadedWallpapers(): List<WallpaperEntity>

    @Query("UPDATE wallpapers_entity SET is_favorite = :isFavorite WHERE wallpaperId = :id")
    abstract fun updateWallpaperFavoriteStatus(id: Int, isFavorite: Int)

    @Query("UPDATE wallpapers_entity SET is_downloaded = :isDownloaded WHERE wallpaperId = :id")
    abstract fun updateWallpaperDownloadStatus(id: Int, isDownloaded: Long)

    @Upsert
    abstract fun upsertWallpaper(wallpaper: WallpaperEntity)

    @Upsert
    abstract fun upsertWallpapers(wallpapers: List<WallpaperEntity>)

    @Query("DELETE FROM wallpapers_entity WHERE wallpaperId = :id")
    abstract fun deleteWallpaperById(id: Int)

    @Query("DELETE FROM wallpapers_entity")
    abstract fun deleteAllWallpapers()


    @Query("SELECT * FROM wallpapers_entity WHERE is_test_data = 0")
    abstract fun observeGetAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("""
SELECT w.*
FROM wallpapers_entity AS w
WHERE w.is_test_data = 0
  AND (
      SELECT COUNT(*)
      FROM wallpapers_entity AS w2
      WHERE w2.category_id = w.category_id
        AND w2.is_test_data = 0
        AND (
             w2.created_at > w.created_at
             OR (w2.created_at = w.created_at AND w2.wallpaperId > w.wallpaperId)
        )
  ) < 5
ORDER BY w.category_id, w.created_at DESC, w.wallpaperId DESC
""")
    abstract fun observeGetNewWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers_entity WHERE wallpaperId = :id")
    abstract fun observeGetWallpaperById(id: Int): Flow<WallpaperEntity?>

    @Query("SELECT * FROM wallpapers_entity WHERE category_id = :id AND is_test_data = 0")
    abstract fun observeGetWallpapersByCategoryId(id: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers_entity WHERE is_favorite = 1 AND is_test_data = 0")
    abstract fun observeGetFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers_entity WHERE is_downloaded > 0 ORDER BY is_downloaded DESC")
    abstract fun observeGetDownloadedWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers_entity WHERE is_featured = 1 AND is_test_data = 0")
    abstract fun observeGetFeaturedWallpapers(): Flow<List<WallpaperEntity>>
    
    @Query("UPDATE wallpapers_entity SET is_for_you = CASE WHEN category_id in (:catId) THEN 1 ELSE 0 END")
    abstract fun updateForYouWallpapersStatus(catId: List<String>)

    @Query("SELECT * FROM wallpapers_entity WHERE is_for_you == 1")
    abstract fun observeForYouWallpapers() : Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers_entity WHERE is_for_you == 1")
    abstract suspend fun getForYouWallpapers(): List<WallpaperEntity>

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE (:categoryId IS NULL OR category_id = :categoryId)
          AND is_test_data = 0
        ORDER BY wallpaperId DESC
        LIMIT :limit OFFSET :offset
    """)
    abstract suspend fun fetchPage(categoryId: String?, limit: Int, offset: Int): List<WallpaperEntity>

    @Query("""
        SELECT wallpaperId, is_favorite AS isFavorite, isSelectedHome, isSelectedLock, access_type AS accessType, is_downloaded AS isDownloaded, is_for_you AS isForYou
        FROM wallpapers_entity
        WHERE wallpaperId IN (:ids)
    """)
    abstract suspend fun getLocalFlags(ids: List<Int>): List<LocalFlags>

    @Query("""
        UPDATE wallpapers_entity
        SET isSelectedLock = CASE
            WHEN wallpaperId = :id THEN 1
            WHEN isSelectedLock = 1 THEN 3
            ELSE isSelectedLock
        END
    """)
    abstract suspend fun applyLockSelection(id: Int): Int

    // --- Apply: đặt làm MÀN HÌNH NỀN ---
    @Query("""
        UPDATE wallpapers_entity
        SET isSelectedHome = CASE
            WHEN wallpaperId = :id THEN 1
            WHEN isSelectedHome = 1 THEN 3
            ELSE isSelectedHome
        END
    """)
    abstract suspend fun applyHomeSelection(id: Int): Int

    @Query("""
        UPDATE wallpapers_entity
        SET 
            isSelectedLock = CASE
                WHEN wallpaperId = :lockId THEN 1
                WHEN isSelectedLock = 1 THEN 3
                ELSE isSelectedLock
            END,
            isSelectedHome = CASE
                WHEN wallpaperId = :homeId THEN 1
                WHEN isSelectedHome = 1 THEN 3
                ELSE isSelectedHome
            END
        WHERE wallpaperId IN (:lockId, :homeId) 
           OR isSelectedLock = 1 
           OR isSelectedHome = 1
    """)
    abstract suspend fun applyBothSelection(lockId: Int, homeId: Int): Int

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE isSelectedLock = 3 OR isSelectedHome = 3
    """)
    abstract suspend fun getHistoryAll(): List<WallpaperEntity>

    /**
     * Tìm theo name/tags/original_name (không phân biệt hoa thường), tuỳ chọn lọc wallpaper_type.
     * Phân trang bằng LIMIT/OFFSET.
     */
    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE (:wallpaperType IS NULL OR wallpaper_type = :wallpaperType)
          AND is_test_data = 0
          AND (
               name  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR tags  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR original_name LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
          )
        ORDER BY created_at DESC, wallpaperId DESC
        LIMIT :limit OFFSET :offset
    """)
    abstract suspend fun searchPage(
        q: String,
        wallpaperType: Int?,      // null = không lọc
        limit: Int,
        offset: Int
    ): List<WallpaperEntity>

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE (:wallpaperType IS NULL OR wallpaper_type = :wallpaperType)
          AND is_test_data = 0
          AND (
               name  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR tags  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR original_name LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
          )
        ORDER BY created_at DESC, wallpaperId DESC
    """)
    abstract fun observeGetSearchWallpapers(
        q: String,
        wallpaperType: Int?      // null = không lọc
    ): Flow<List<WallpaperEntity>>
    /**
     * (tuỳ chọn) Đếm tổng kết quả để biết còn trang nữa không.
     */
    @Query("""
        SELECT COUNT(*) FROM wallpapers_entity
        WHERE (:wallpaperType IS NULL OR wallpaper_type = :wallpaperType)
          AND is_test_data = 0
          AND (
               name  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR tags  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR original_name LIKE '%' || :q || '%' ESCAPE '\\' COLLATE NOCASE
          )
    """)
    abstract suspend fun countSearch(
        q: String,
        wallpaperType: Int?
    ): Int

    /**
     * Search with option to include test data (for developer mode)
     */
    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE (:wallpaperType IS NULL OR wallpaper_type = :wallpaperType)
          AND (:includeTestData = 1 OR is_test_data = 0)
          AND (
               name  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR tags  LIKE '%' || :q || '%' ESCAPE '\' COLLATE NOCASE
            OR original_name LIKE '%' || :q || '%' ESCAPE '\\' COLLATE NOCASE
          )
        ORDER BY created_at DESC, wallpaperId DESC
        LIMIT :limit OFFSET :offset
    """)
    abstract suspend fun searchPageWithTestData(
        q: String,
        wallpaperType: Int?,
        includeTestData: Int,  // 1 = include, 0 = exclude
        limit: Int,
        offset: Int
    ): List<WallpaperEntity>

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE isSelectedLock IN (1, 3) OR isSelectedHome IN (1, 3)
        ORDER BY wallpaperId DESC
    """)
    abstract fun observeHistoryWallpapers(): Flow<List<WallpaperEntity>>

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE isSelectedLock = 1 OR isSelectedHome = 1
    """)
    abstract fun observeCurrentWallpapers(): Flow<List<WallpaperEntity>>

    @Query("""
        UPDATE wallpapers_entity
        SET isSelectedLock = CASE WHEN isSelectedLock = 3 THEN 0 ELSE isSelectedLock END,
            isSelectedHome = CASE WHEN isSelectedHome = 3 THEN 0 ELSE isSelectedHome END
        WHERE wallpaperId = :id
    """)
    abstract suspend fun removeFromHistory(id: Int)

    @Query(
        """
        SELECT * FROM wallpapers_entity WHERE (:categoryId IS NULL OR category_id = :categoryId) AND is_test_data = 0 
    ORDER BY
    CASE WHEN no_shuffle = 1 THEN 0 ELSE 1 END,
    CASE 
    WHEN no_shuffle = 1 THEN -created_at
    ELSE abs((wallpaperId * 1103515245 + :sessionSeed + :categoryHash) % 1000000007)
    END,
    wallpaperId                                                                   
        LIMIT :limit OFFSET :offset
    """
    )
    abstract suspend fun fetchPageOrdered(
        categoryId: String?,
        sessionSeed: Long,
        categoryHash: Long,
        limit: Int,
        offset: Int
    ): List<WallpaperEntity>

    @Query("""
        SELECT * FROM wallpapers_entity
        WHERE category_id = :categoryId
          AND is_test_data = 0
        ORDER BY
          CASE WHEN no_shuffle = 1 THEN 0 ELSE 1 END,
          CASE 
            WHEN no_shuffle = 1 THEN -created_at
            ELSE abs((wallpaperId * 1103515245 + :sessionSeed + :categoryHash) % 1000000007)
          END,
          wallpaperId
        LIMIT 2
    """)
    abstract suspend fun pickTwoFromCategory(
        categoryId: String,
        sessionSeed: Long,
        categoryHash: Long
    ): List<WallpaperEntity>

    @Query("SELECT DISTINCT category_id FROM wallpapers_entity WHERE is_test_data = 0")
    abstract suspend fun getAllCategoryIds(): List<String>
}