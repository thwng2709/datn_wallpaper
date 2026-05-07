package com.itsthwng.twallpaper.repository

import com.itsthwng.twallpaper.data.dao.WallpaperDao
import com.itsthwng.twallpaper.data.entity.WallpaperEntity
import com.itsthwng.twallpaper.data.dao.ZipperDao
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.isZipper
import com.itsthwng.twallpaper.data.model.toWallpaperEntity
import com.itsthwng.twallpaper.data.model.toWallpapersItem
import com.itsthwng.twallpaper.di.SessionManager
import com.itsthwng.twallpaper.data.model.toZipperEntity
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.preview.PreviewActivity
import com.itsthwng.twallpaper.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import kotlin.String

class WallpapersRepositoryImp @Inject constructor(
    private val wallpaperDao: WallpaperDao,
    private val zipperDao: ZipperDao,
    private val zipperRepository: ZipperRepository,
    private val session: SessionManager,
    private val localStorage: LocalStorage
): WallpapersRepository{
    override suspend fun getWallpapers(): List<SettingData.WallpapersItem> {
        return try {
            wallpaperDao.getAllWallpapers().map { it.toWallpapersItem() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getWallPapersByCategory(categoryId: String): List<SettingData.WallpapersItem> {
        return try {
            wallpaperDao.getWallpapersByCategoryId(categoryId).map { it.toWallpapersItem() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFeaturesWallpapers(): List<SettingData.WallpapersItem> {
        return try {
            // Lấy tất cả wallpaper và lọc ra những cái có is_featured = 1
            wallpaperDao.getAllWallpapers()
                .filter { it.isFeatured == 1 }
                .map { it.toWallpapersItem() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getFavoriteWallpapers(): List<SettingData.WallpapersItem> {
        return try {
            wallpaperDao.getFavoriteWallpapers().map { it.toWallpapersItem() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getDownloadedWallpapers(): List<SettingData.WallpapersItem> {
        return try {
            wallpaperDao.getDownloadedWallpapers().map { it.toWallpapersItem() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateWallpaper(wallpaper: SettingData.WallpapersItem) {
        try {
            wallpaperDao.upsertWallpaper(wallpaper.toWallpaperEntity())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun saveWallpapers(wallpapers: List<SettingData.WallpapersItem>) {
        try {
            val wallpaperEntities = wallpapers.map { it.toWallpaperEntity() }
            wallpaperDao.upsertWallpapers(wallpaperEntities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteAllWallpapers() {
        try {
            wallpaperDao.deleteAllWallpapers()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteWallpaperById(id: String) {
        try {
            wallpaperDao.deleteWallpaperById(id.toIntOrNull() ?: 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun observeWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetAllWallpapers().map { list -> list.map { it.toWallpapersItem() } }

    override fun observeNewWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetNewWallpapers().map { list ->
            val orderStr = localStorage.categorySort
            val rank = orderStr.toCategoryRankMap()
            list.sortedWith(
                compareBy<WallpaperEntity> { rank[it.categoryId] ?: Int.MAX_VALUE }
                    .thenByDescending { it.createdAt }  // tie-break hợp lý cho "New"
                    .thenBy { it.wallpaperId }
            ).map { it.toWallpapersItem() }
        }

    override fun observeWallpapersByCategory(categoryId: String): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetWallpapersByCategoryId(categoryId).map { list -> list.map { it.toWallpapersItem() } }

    override fun observeFeaturedWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetFeaturedWallpapers().map { list -> list.map { it.toWallpapersItem() } }

    override fun observeRandomFeaturedWallpapers(
        onlyCategoryIds: Set<String>?
    ): Flow<List<SettingData.WallpapersItem>> {
        val categoryOrder = localStorage.categorySort
        val rank = categoryOrder.toCategoryRankMap()
        return wallpaperDao.observeGetAllWallpapers()
            .combine(session.seed) { all, seed ->
                val grouped = all.groupBy { it.categoryId }
                val sortedCats = grouped.keys.sortedWith(
                    compareBy<String> { rank[it] ?: Int.MAX_VALUE }.thenBy { it }
                )

                val out = ArrayList<WallpaperEntity>(sortedCats.size * 2)
                for (cat in sortedCats) {
                    val items = grouped[cat].orEmpty()
                    val picked = if (cat == "couple") {
                        pickTwoFromCoupleCategory(items, seed)          // luôn trả ≤ 2
                    } else {
                        pickTwoNormalCategory(items, seed, cat)         // luôn trả ≤ 2
                    }
                    out.addAll(picked)
                }

                out.map { it.toWallpapersItem() }
            }
            // tránh emit thừa: so sánh theo sequence ID
            .distinctUntilChanged { old, new ->
                old.size == new.size &&
                        old.asSequence().map { it.id }
                            .zip(new.asSequence().map { it.id })
                            .all { (a, b) -> a == b }
            }
    }

    private fun orderFeaturedByCategoryRank(
        list: List<WallpaperEntity>,
        rank: Map<String, Int>,
        seed: Long
    ): List<WallpaperEntity> {
        if (list.isEmpty()) return emptyList()

        val grouped = list.groupBy { it.categoryId }

        // Sắp category theo rank từ categorySort; cái nào không có rank -> xuống cuối
        val sortedCats = grouped.keys.sortedWith(
            compareBy<String> { rank[it] ?: Int.MAX_VALUE }
                .thenBy { it } // tie-break ổn định
        )

        val out = ArrayList<WallpaperEntity>(list.size)
        for (cat in sortedCats) {
            val items = grouped[cat] ?: continue
            val orderedWithin = if (cat == "couple") {
                reorderForSessionCouple(items, seed)          // GIỮ CẶP
            } else {
                reorderForSessionNormal(items, seed, cat)     // pin + shuffle theo seed
            }
            out.addAll(orderedWithin)
        }
        return out
    }

    // ---- String -> rank map helper ----
    private fun String?.toCategoryRankMap(): Map<String, Int> {
        if (this.isNullOrBlank()) return emptyMap()
        return this.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .withIndex()
            .associate { it.value to it.index }
    }

    private fun pickTwoPerCategoryWithCoupleRule(
        all: List<WallpaperEntity>,
        sessionSeed: Long
    ): List<WallpaperEntity> {
        if (all.isEmpty()) return emptyList()

        return all.groupBy { it.categoryId }
            .values
            .flatMap { items ->
                val catId = items.first().categoryId
                if (catId == "couple") {
                    pickTwoFromCoupleCategory(items, sessionSeed)
                } else {
                    pickTwoNormalCategory(items, sessionSeed, catId)
                }
            }
    }

    private fun pickTwoNormalCategory(
        items: List<WallpaperEntity>,
        sessionSeed: Long,
        catId: String
    ): List<WallpaperEntity> {
        val (pinned, normal) = items.partition { it.noShuffle }

        val pinnedSorted = pinned.sortedWith(
            compareByDescending<WallpaperEntity> { it.createdAt }
                .thenBy { it.wallpaperId }
        )
        val normalSorted = normal.sortedBy { shuffleKey(sessionSeed, catId, it.wallpaperId.toLong()) }

        return (pinnedSorted + normalSorted).take(2)
    }

    // === COUPLE category: random theo CẶP, mỗi cặp là 2 item liên tiếp theo wallpaperId ===
    private fun pickTwoFromCoupleCategory(
        items: List<WallpaperEntity>,
        sessionSeed: Long
    ): List<WallpaperEntity> {
        // 1) Sort theo wallpaperId rồi tạo cặp 2-2
        val sorted = items.sortedBy { it.wallpaperId }
        val pairs = sorted.chunked(2).filter { it.size == 2 }
        if (pairs.isEmpty()) {
            // Fallback: nếu dữ liệu lẻ không đủ tạo cặp, cứ trả về tối đa 2 theo thứ tự cũ
            return sorted.take(2)
        }

        // 2) Pinned-pairs: nếu 1 trong 2 item có noShuffle => cả cặp được coi là pinned
        val (pinnedPairs, normalPairs) = pairs.partition { p ->
            p[0].noShuffle || p[1].noShuffle
        }

        // Tie-break cho pinned: cặp có createdAt mới hơn trước; rồi đến wallpaperId nhỏ hơn
        val chosenPinned = pinnedPairs.sortedWith(
            compareByDescending<List<WallpaperEntity>> { maxCreatedAt(it) }
                .thenBy { minId(it) }
        ).firstOrNull()
        if (chosenPinned != null) return chosenPinned

        // 3) Không có pinned ⇒ chọn 1 cặp theo sessionSeed (deterministic)
        //    Sort các cặp theo shuffleKey của "khóa cặp"
        val catId = "couple"
        val normalSorted = normalPairs.sortedBy { p ->
            val pairKey = pairMixId(p) // khóa ID cho cặp
            shuffleKey(sessionSeed, catId, pairKey)
        }

        return normalSorted.first()
    }

    private fun maxCreatedAt(pair: List<WallpaperEntity>): Long =
        maxOf(pair[0].createdAt, pair[1].createdAt)

    private fun minId(pair: List<WallpaperEntity>): Int =
        minOf(pair[0].wallpaperId, pair[1].wallpaperId)

    private fun pairMixId(pair: List<WallpaperEntity>): Long {
        // Trộn 2 id để làm "khóa cặp" ổn định
        val a = pair[0].wallpaperId.toLong()
        val b = pair[1].wallpaperId.toLong()
        // Mix đơn giản nhưng đủ ổn định
        return a * 1103515245L + b * 12345L
    }


    override fun observeFavoriteWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetFavoriteWallpapers().map { list -> list.map { it.toWallpapersItem() } }

    override fun observeDownloadedWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        wallpaperDao.observeGetDownloadedWallpapers().map { list -> list.map { it.toWallpapersItem() } }

    override fun observeSearchKeyword(
        keyword: String,
        wallpaperType: Int?
    ): Flow<List<SettingData.WallpapersItem>> {
        return wallpaperDao.observeGetSearchWallpapers(keyword, wallpaperType).map { list -> list.map { it.toWallpapersItem() } }
    }

    override suspend fun updateWallpaperDownloadStatus(id: Int, isDownloaded: Long) {
        wallpaperDao.updateWallpaperDownloadStatus(id, isDownloaded)
    }

    override suspend fun updateSelectedWallpaper(wallpapersItem: SettingData.WallpapersItem, type: Int) {
        wallpapersItem.id ?: return

        try {
            if (wallpapersItem.isZipper()) {
                // Delegate to ZipperRepository (proper layer separation)
                zipperRepository.updateSelectedZipper(wallpapersItem.toZipperEntity(), type)
            } else {
                // Regular wallpaper handling
                wallpaperDao.upsertWallpaper(wallpapersItem.toWallpaperEntity())
                applyWallpaperSelection(wallpaperDao, type, wallpapersItem.id)
            }
        } catch (e: Exception) {
            Logger.e("WallpapersRepository", "updateSelectedWallpaper failed: ${e.message}")
        }
    }

    /**
     * Helper function to apply wallpaper selection (DRY principle)
     * Extracted to avoid code duplication
     */
    private suspend fun applyWallpaperSelection(dao: WallpaperDao, type: Int, id: Int) {
        when(type) {
            PreviewActivity.HOME -> dao.applyHomeSelection(id)
            PreviewActivity.LOCK -> dao.applyLockSelection(id)
            PreviewActivity.BOTH -> dao.applyBothSelection(id, id)
            else -> {} // No-op for unknown types
        }
    }

    override suspend fun searchPage(
        keyword: String,
        wallpaperType: Int?,
        page: Int,
        pageSize: Int
    ): List<SettingData.WallpapersItem> {
        val q = keyword.escapeSqlLike()
        val offset = (page - 1) * pageSize
        return wallpaperDao.searchPage(q, wallpaperType, pageSize, offset)
            .map { it.toWallpapersItem() }
    }

    override suspend fun countSearch(keyword: String, wallpaperType: Int?): Int {
        val q = keyword.escapeSqlLike()
        return wallpaperDao.countSearch(q, wallpaperType)
    }

    override suspend fun getHistoryWallpapers(): List<SettingData.WallpapersItem> {
        return try {
            val wallpapers = wallpaperDao.getHistoryAll().map { it.toWallpapersItem() }
            val zippers = zipperDao.getHistoryAll().map { it.toWallpapersItem() }
            (wallpapers + zippers).sortedByDescending { it.id }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override fun observeHistoryWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        combine(
            wallpaperDao.observeHistoryWallpapers(),
            zipperDao.observeHistoryZippers()
        ) { wallpapers, zippers ->
            val wallpaperItems = wallpapers.map { it.toWallpapersItem() }
            val zipperItems = zippers.map { it.toWallpapersItem() }
            (wallpaperItems + zipperItems).sortedByDescending { it.id }
        }

    override fun observeCurrentWallpapers(): Flow<List<SettingData.WallpapersItem>> =
        combine(
            wallpaperDao.observeCurrentWallpapers(),
            zipperDao.observeCurrentZippers()
        ) { wallpapers, zippers ->
            val wallpaperItems = wallpapers.map { it.toWallpapersItem() }
            val zipperItems = zippers.map { it.toWallpapersItem() }
            (wallpaperItems + zipperItems).sortedByDescending { it.id }
        }

    override suspend fun removeFromHistory(wallpaperId: Int, isZipper: Boolean) {
        try {
            if (isZipper) {
                zipperDao.removeFromHistory(wallpaperId)
            } else {
                wallpaperDao.removeFromHistory(wallpaperId)
            }
        } catch (e: Exception) {
            Logger.e("WallpapersRepository", "removeFromHistory failed: ${e.message}")
        }
    }

    // -------------------- Cache --------------------
    private val caches = mutableMapOf<String, OrderedCache>()
    private val mutex = Mutex()

    // -------------------- Màn 1 (FULL) --------------------
    override fun observeWallpapersOrdered(categoryId: String): Flow<List<SettingData.WallpapersItem>> {
        return wallpaperDao.observeGetWallpapersByCategoryId(categoryId)
            .combine(session.seed) { list, seed ->
                // Sắp xếp ổn định theo session; riêng "couple" sẽ theo CẶP
                val orderedEntities = reorderForSession(list, seed, categoryId)
                orderedEntities.map { it.toWallpapersItem() }
            }
            .onEach { ordered ->
                // Lưu snapshot cho paging dùng
                mutex.withLock {
                    caches[categoryId] = OrderedCache(session.seed.value, ordered)
                }
            }
    }

    // -------------------- Màn 2 (PAGING) --------------------
    override suspend fun loadPage(
        categoryId: String?,
        page: Int,
        pageSize: Int
    ): List<SettingData.WallpapersItem> {
        if (categoryId == null) return emptyList()
        val offset = (page - 1).coerceAtLeast(0) * pageSize

        // 1) Dùng snapshot nếu còn hợp lệ (cùng session)
        val cached = mutex.withLock { caches[categoryId] }
        if (cached != null && cached.sessionSeed == session.seed.value) {
            return cached.ordered.drop(offset).take(pageSize)
        }

        // 2) Chưa có snapshot -> build nhanh một lần cho category này
        val raw = wallpaperDao.getWallpapersByCategoryId(categoryId) // one-shot
        val entitiesOrdered = reorderForSession(raw, session.seed.value, categoryId)
        val ordered = entitiesOrdered.map { it.toWallpapersItem() }

        mutex.withLock {
            caches[categoryId] = OrderedCache(session.seed.value, ordered)
        }
        return ordered.drop(offset).take(pageSize)
    }

    // -------------------- Helpers --------------------
    private fun reorderForSession(
        list: List<WallpaperEntity>,
        seed: Long,
        categoryId: String
    ): List<WallpaperEntity> {
        return if (categoryId == "couple") {
            // Giữ cặp theo wallpaperId: [0,1], [2,3], ...
            reorderForSessionCouple(list, seed)
        } else {
            reorderForSessionNormal(list, seed, categoryId)
        }
    }

    // Các category bình thường: pin noShuffle lên đầu, còn lại shuffle theo seed
    private fun reorderForSessionNormal(
        list: List<WallpaperEntity>,
        seed: Long,
        categoryId: String
    ): List<WallpaperEntity> {
        val (fixed, rest) = list.partition { it.noShuffle }

        val fixedSorted = fixed.sortedWith(
            compareByDescending<WallpaperEntity> { it.createdAt }
                .thenBy { it.wallpaperId }
        )

        val restSorted = rest.sortedBy { shuffleKey(seed, categoryId, it.wallpaperId.toLong()) }
        return fixedSorted + restSorted
    }

    // Category "couple": sắp xếp theo CẶP (2 ảnh liên tiếp theo wallpaperId là một cặp)
    private fun reorderForSessionCouple(
        list: List<WallpaperEntity>,
        seed: Long
    ): List<WallpaperEntity> {
        if (list.isEmpty()) return emptyList()

        // 1) Sort theo wallpaperId rồi tạo cặp
        val byId = if (localStorage.langCode == "ar") {
            list.sortedByDescending { it.wallpaperId }
        } else {
            list.sortedBy { it.wallpaperId }
        }
        val pairs = byId.chunked(2).filter { it.size == 2 }
        val dangling = if (byId.size % 2 == 1) byId.last() else null // ảnh lẻ (nếu có)

        // 2) Pinned-pairs: nếu 1 trong 2 item có noShuffle => cả cặp pinned
        val (pinnedPairs, normalPairs) = pairs.partition { pair ->
            pair[0].noShuffle || pair[1].noShuffle
        }

        // Pinned pairs: mới nhất trước (theo max createdAt), tie-break theo min wallpaperId
        val pinnedSorted = pinnedPairs.sortedWith(
            compareByDescending<List<WallpaperEntity>> { maxCreatedAt(it) }
                .thenBy { minId(it) }
        )

        // 3) Các cặp còn lại: shuffle theo seed (khóa là "pairKey")
        val catId = "couple"
        val normalSorted = normalPairs.sortedBy { pair ->
            val pairKey = pairMixId(pair) // trộn 2 id để có khóa ổn định
            shuffleKey(seed, catId, pairKey)
        }

        // 4) Ghép lại: flatten về danh sách entity theo thứ tự cặp
        val result = ArrayList<WallpaperEntity>(byId.size)
        pinnedSorted.forEach { result.addAll(it) }
        normalSorted.forEach { result.addAll(it) }

        // 5) Ảnh lẻ (nếu có):
        //  - nếu noShuffle -> đưa ngay sau pinned-pairs
        //  - nếu không -> đưa cuối (sau normal-pairs)
        dangling?.let { single ->
            if (single.noShuffle) {
                // chèn ngay sau phần pinned
                val insertIndex = pinnedSorted.sumOf { it.size } // = 2 * số pinned-pairs
                result.add(insertIndex.coerceIn(0, result.size), single)
            } else {
                result.add(single)
            }
        }

        return result
    }

    private fun shuffleKey(seed: Long, categoryId: String, key: Long): Double {
        val mix = seed xor categoryId.hashCode().toLong() xor key
        val rnd = java.util.Random(mix) // deterministic trong session
        return rnd.nextDouble()
    }

    private data class OrderedCache(
        val sessionSeed: Long,
        val ordered: List<SettingData.WallpapersItem>
    )

    /** Escape ký tự wildcard cho LIKE: %, _ và \  */
    private fun String.escapeSqlLike(): String =
        this.trim()
            .replace("\\", "\\\\")
            .replace("%", "\\%")
            .replace("_", "\\_")
}