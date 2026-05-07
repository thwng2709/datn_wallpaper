package com.itsthwng.twallpaper.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.itsthwng.twallpaper.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CategoryDao {
    @Insert
    abstract fun insertCategories(categories: List<CategoryEntity>)

    @Upsert
    abstract fun upsertCategory(category: CategoryEntity)

    @Upsert
    abstract fun upsertCategories(categories: List<CategoryEntity>)

    @Query("SELECT categoryId FROM category_entity")
    abstract suspend fun getAllIds(): List<String>

    @Query("SELECT * FROM category_entity ORDER BY position ASC")
    abstract fun getAllCategories(): List<CategoryEntity>

    @Query("DELETE FROM category_entity")
    abstract fun deleteAllCategories()

    @Query("SELECT * FROM category_entity WHERE wallpapers_count != 0 ORDER BY position ASC")
    abstract fun observeAll(): Flow<List<CategoryEntity>>

    @Query("UPDATE category_entity SET position = :position WHERE lower(categoryId) = lower(:id)")
    abstract suspend fun updateOnePosition(id: String, position: Int): Int
}

suspend fun CategoryDao.updatePositionsInBulk(orderedIds: List<String>) {
    if (orderedIds.isEmpty()) return

    // Tạo VALUES ('id1', 0), ('id2', 1), ...
    val values = orderedIds.mapIndexed { idx, id -> "('$id',$idx)" }.joinToString(",")
    val inList = orderedIds.joinToString(",") { "'$it'" }

    orderedIds.forEachIndexed { idx, id ->
        this.updateOnePosition(id, idx)
    }
}