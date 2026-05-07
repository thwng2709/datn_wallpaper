package com.itsthwng.twallpaper.utils.wallpaper

import com.itsthwng.twallpaper.data.entity.CategoryEntity
import com.itsthwng.twallpaper.data.model.SettingData.WallpapersItem
import com.itsthwng.twallpaper.workManager.OrderConf

object CategoryHelper {

    fun sortCategoriesByOrder(
        categories: List<CategoryEntity>,
        order: OrderConf
    ): List<CategoryEntity> {
        // Map: normalizedId -> index trong order
        val priorityMap: Map<String, Int> = order.categories_order
            ?.flatMapIndexed { index, conf ->
                (sequenceOf(conf.canonicalId) + conf.aliases.asSequence())
                    .map { normalizeId(it!!) to index }
            }
            ?.toMap()
            ?: emptyMap()

        return categories.sortedWith(compareBy(
            { priorityMap[normalizeId(it.categoryId)] ?: Int.MAX_VALUE }, // ưu tiên theo order
            { it.categoryId } // fallback: sort theo id để ổn định
        ))
    }

    fun sortWallpapersByCategoryOrder(
        wallpapers: List<WallpapersItem>,
        order: OrderConf
    ): List<WallpapersItem> {
        // Map: normalized categoryId -> index trong order
        val priorityMap: Map<String, Int> = order.categories_order
            ?.flatMapIndexed { index, conf ->
                (sequenceOf(conf.canonicalId) + conf.aliases.asSequence())
                    .filter { it != null }
                    .map { normalizeId(it!!) to index }
            }
            ?.toMap()
            ?: emptyMap()

        return wallpapers.sortedWith(compareBy(
            { priorityMap[normalizeId(it.categoryId ?: "")] ?: Int.MAX_VALUE }, // theo order
            { it.id ?: Int.MAX_VALUE } // fallback: sort theo id trong cùng 1 category
        ))
    }

    // Hàm normalize giống như trước
    fun normalizeId(id: String): String =
        id.lowercase().trim().replace(" ", "_")
}