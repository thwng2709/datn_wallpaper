package com.itsthwng.twallpaper.workManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.room.withTransaction
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.AppDatabase
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.dao.CategoryDao
import com.itsthwng.twallpaper.data.dao.WallpaperDao
import com.itsthwng.twallpaper.data.dao.ZipperDao
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.SettingData.CategoriesItem
import com.itsthwng.twallpaper.data.model.toCategoriesItem
import com.itsthwng.twallpaper.data.model.toCategoryEntity
import com.itsthwng.twallpaper.data.model.toWallpaperEntity
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.CHAINS
import com.itsthwng.twallpaper.utils.Constants.ZIPPERS
import com.itsthwng.twallpaper.utils.Constants.ZIPPER_IMAGE
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.workManager.model.LocaleDataModel
import com.itsthwng.twallpaper.workManager.model.toCategoryItem
import com.itsthwng.twallpaper.workManager.model.toWallpaperItem
import com.itsthwng.twallpaper.workManager.model.toZipperImageEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.tasks.await
import java.text.Normalizer
import java.util.Locale

@HiltWorker
class LocaleSyncWorker@AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val categoryDao: CategoryDao,
    private val wallpaperDao: WallpaperDao,
    private val zipperDao: ZipperDao,
    private val db: AppDatabase
) : CoroutineWorker(appContext, params) {

    private val gson = Gson()
    private val storage by lazy { Firebase.storage } // firebase-storage-ktx


    companion object {
        private const val CHANNEL_ID = "locale_sync_channel"
        private const val CHANNEL_NAME = "Locale Sync"
        private const val NOTIFICATION_ID = 1001
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle(applicationContext.getString(R.string.syncing_language_data))
            .setContentText(applicationContext.getString(R.string.updating_app_content))
            .setSmallIcon(R.drawable.ic_information)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Language data synchronization"
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun doWork(): Result {
        val locale = inputData.getString("locale") ?: return Result.failure()

        try {
            val jsonText = when(locale){
                "vi" -> CommonInfo.currentData_Vi
                "en" -> CommonInfo.currentData_En
                "fr" -> CommonInfo.currentData_Fr
                "es" -> CommonInfo.currentData_Es
                "de" -> CommonInfo.currentData_De
                "ar" -> CommonInfo.currentData_Ar
                "zh" -> CommonInfo.currentData_Zh
                "ja" -> CommonInfo.currentData_Ja
                "ko" -> CommonInfo.currentData_Ko
                "pt" -> CommonInfo.currentData_Pt
                "ru" -> CommonInfo.currentData_Ru
                "tr" -> CommonInfo.currentData_Tr
                "uk" -> CommonInfo.currentData_Uk
                "hi" -> CommonInfo.currentData_En
                else -> return Result.failure() // Không hỗ trợ ngôn ngữ này
            }

//            val localeData = try {
//                gson.fromJson(jsonText, LocaleDataModel::class.java)
//            } catch (e : Exception){
//                return Result.failure()
//            }
            var localeData = fetchWallpapers(locale)

            if (localeData == null) {
                localeData = try {
                    gson.fromJson(jsonText, LocaleDataModel::class.java)
                } catch (e: Exception) {
                    return Result.failure()
                }
            }
            // Debug log để kiểm tra data từ Firebase
            Logger.d("=== FIREBASE DATA DEBUG ===")
            Logger.d("Locale: $locale")
            Logger.d("Categories count: ${localeData?.categories?.size}")
            Logger.d("ZipperImages count: ${localeData?.zipperImages?.size ?: 0}")
            Logger.d("Zippers count: ${localeData?.zippers?.size ?: 0}")
            Logger.d("Chains count: ${localeData?.chains?.size ?: 0}")
            val localStorage = LocalData(applicationContext, "sharedPreferences")
            val baseUrl = localeData?.baseUrl ?: ""

            val categories = localeData?.categories?.map { it.toCategoryItem(baseUrl = baseUrl) }
            db.withTransaction {
                categories?.map { it.toCategoryEntity() }?.let { categoryDao.upsertCategories(it) }
            }
            setProgress(workDataOf("stage" to "categories_done", "progress" to 20))

            val orderJson = CommonInfo.categoryOrderJson // file JSON thứ tự + aliases
            val orderConf = gson.fromJson(orderJson, OrderConf::class.java)

            db.withTransaction {
                val categorySort = applyOrderByContainment(categoryDao, orderConf)
                localStorage.categorySort = categorySort.joinToString(",")
            }

            val categoriesSaved = categoryDao.getAllCategories()
            localStorage.saveCategories(categoriesSaved.map { it.toCategoriesItem() })
            // 3) Với từng category → tải images.json → map & lưu
            val total = categories?.size?.coerceAtLeast(1)
            var done = 0
            var allWallpapers: MutableList<SettingData.WallpapersItem> = mutableListOf()

            for (cat in localeData?.categories!!) {

                val walls = cat.images.map { it.toWallpaperItem(baseUrl = baseUrl) }
                if (walls.isNotEmpty()) {
                    allWallpapers.addAll(walls)
                    val entities = walls.map { it.toWallpaperEntity() }

                    val ids = entities.map { it.wallpaperId }
                    val localFlags = wallpaperDao.getLocalFlags(ids)
                    val flagsMap = localFlags.associateBy { it.wallpaperId }

                    entities.forEach { e ->
                        flagsMap[e.wallpaperId]?.let { f ->
                            e.isFavorite = f.isFavorite
                            e.isSelectedHome = f.isSelectedHome
                            e.isSelectedLock = f.isSelectedLock
                            e.accessType = f.accessType
                            if(f.accessType == Constants.IMAGE_TYPE_FREE){
                                e.pricePoints = 0
                            }
                        }
                    }
                    db.withTransaction {
                        wallpaperDao.upsertWallpapers(entities)
                    }
                }

                done++
                val pct = 20 + (done * 60 / total!!) // từ 20% → 80%
                setProgress(workDataOf("stage" to "images_progress", "progress" to pct))
                if (isStopped) return Result.retry()
            }

            if(allWallpapers.isNotEmpty()){
                val zipperImages = allWallpapers
                val listZipperEntity = zipperImages.map { it.toZipperImageEntity(ZIPPER_IMAGE) }
                val ids = listZipperEntity.map { it.id }
                val localFlags = zipperDao.getLocalFlags(ids)
                val flagsMap = localFlags.associateBy { it.wallpaperId }

                listZipperEntity.forEach { e ->
                    flagsMap[e.id]?.let { f ->
                        e.accessType = f.accessType
                        e.isSelectedHome = f.isSelectedHome
                        e.isSelectedLock = f.isSelectedLock
                    }
                }
                db.withTransaction {
                    zipperDao.upsertZippers(listZipperEntity)
                }
            }

            localeData.zippers?.let {
                val zipperImages = it
                val listZipperEntity = zipperImages.map { it.toZipperImageEntity(ZIPPERS, baseUrl = baseUrl) }
                db.withTransaction {
                    zipperDao.upsertZippers(listZipperEntity)
                }
            }

            localeData.chains?.let {
                val chainImages = it
                val listZipperEntity = chainImages.map { it.toZipperImageEntity(CHAINS, baseUrl = baseUrl) }
                db.withTransaction {
                    zipperDao.upsertZippers(listZipperEntity)
                }
            }

            localStorage.currentVersionData = CommonInfo.currentVersionData
            localStorage.currentVersionDataR2 = CommonInfo.current_version_data_cloudflare_R2
            localStorage.currentLocale = locale
            return Result.success()
        } catch (e: Exception) {
            Logger.e("LocaleSyncWorker error: ${e.message}")
            return Result.retry()
        }
    }

    // ---- helpers ----

    private suspend fun fetchCategories(locale: String): List<CategoriesItem> {
        val ref = storage.reference.child("$locale/categories.json")
        val bytes = ref.getBytes(2_000_000).await() // 2MB, chỉnh tuỳ kích thước
        val json = bytes.decodeToString()

        // Fallback: parse dạng mảng thuần [ {...}, {...} ]
        val type = object : TypeToken<List<CategoriesItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    private suspend fun fetchWallpapers(locale: String): LocaleDataModel? {
        try {
//            val ref =  Firebase.storage("gs://release-wallpaper.firebasestorage.app").reference.child("all_data_${locale}.json")
//            ref.getBytes(2 * 1024 * 1024).addOnSuccessListener { bytes ->
//                Log.d("Storage", "bytes=${bytes?.size ?: -1}")
//                try {
//                    val json = bytes.toString(Charsets.UTF_8)
//                    Log.d("Storage", "jsonHead=" + json.take(120))
//                    val obj = gson.fromJson(json, LocaleDataModel::class.java)
//                    Log.d("Storage", "parsed=" + (obj != null))
//                    // ... dùng obj
//                } catch (e: Exception) {
//                    Log.e("Storage", "parse error", e)
//                }
//            }.addOnFailureListener { e ->
//                if (e is com.google.firebase.storage.StorageException) {
//                    Log.e("Storage", "code=${e.errorCode}, http=${e.httpResultCode}", e)
//                } else {
//                    Log.e("Storage", "other error", e)
//                }
//            }
            val ref = storage.reference.child("data_${locale}.json")
            val bytes = ref.getBytes(5 * 1024 * 1024).await() // 5MB
            val json = bytes.toString(Charsets.UTF_8)
            var obj: LocaleDataModel? = gson.fromJson(json, LocaleDataModel::class.java)
            return obj
        } catch (e : Exception){
            Logger.e("fetchWallpapers error: ${e.message}")
            return null
        }
    }

    suspend fun applyOrderByContainment(
        categoryDao: CategoryDao,
        order: OrderConf
    ): List<String> {
        // 1) Lấy toàn bộ id hiện có trong DB
        val dbIds: List<String> = categoryDao.getAllIds()

        // Map: normalized -> real DB id (ưu tiên id thực trong DB để update)
        val dbIdByNorm: MutableMap<String, String> =
            dbIds.associateBy { normalizeId(it) }.toMutableMap()

        val usedDbIds = mutableSetOf<String>()
        val resultOrder = mutableListOf<String>() // <-- danh sách kết quả theo thứ tự

        var pos = 0
        order.categories_order?.forEach { conf ->
            // Tập hợp các id hợp lệ cho mục này (canonical + aliases), đã normalize
            val validNorms: Set<String> =
                (sequenceOf(conf.canonicalId) + conf.aliases.asSequence())
                    .map { normalizeId(it) }
                    .toSet()

            // Tìm id trong DB thuộc tập validNorms
            val chosenDbId: String? = validNorms
                .asSequence()
                .mapNotNull { dbIdByNorm[it] }      // chuyển norm -> real DB id
                .firstOrNull { it !in usedDbIds }   // tránh trùng

            if (chosenDbId != null) {
                val affected = categoryDao.updateOnePosition(chosenDbId, pos++)
                usedDbIds += chosenDbId
                resultOrder += chosenDbId           // <-- ghi nhận thứ tự
            }
        }

        val leftovers = dbIds.filter { it !in usedDbIds }
        leftovers.forEach { leftoverId ->
            val affected = categoryDao.updateOnePosition(leftoverId, pos++)
            resultOrder += leftoverId               // <-- bổ sung phần còn lại
            if (affected == 0) Logger.d("⚠️ LEFTOVER not updated: $leftoverId")
        }
        return resultOrder
    }

    private fun resolveCategoryFolder(cat: CategoriesItem): String {
        // Ưu tiên key/originalName; nếu thiếu, tạo slug từ name
        return cat.id?.trim().orEmpty()
            .ifEmpty { cat.originalTitle?.trim().orEmpty() }
    }
}

@Keep
data class OrderConf(val categories_order: List<CategoryConf> ?= null)
@Keep
data class CategoryConf(
    val canonicalId: String ?= null,
    val name: String ?= null,
    val aliases: List<String> = emptyList()
)

private val RE_COMBINING = Regex("\\p{M}+")
private val RE_NON_ALNUM = Regex("[^a-z0-9]+")
private val RE_MULTI_US  = Regex("_+")
fun normalizeId(raw: String?): String {
    if (raw.isNullOrBlank()) return ""

    val noDiacritics = Normalizer.normalize(raw, Normalizer.Form.NFD)
        .replace("đ", "d", ignoreCase = true)     // xử lý cả đ và Đ
        .replace(RE_COMBINING, "")                // bỏ dấu

    return noDiacritics
        .lowercase(Locale.ROOT)                   // tránh issue i/I (tiếng Thổ Nhĩ Kỳ)
        .replace(RE_NON_ALNUM, "_")
        .replace(RE_MULTI_US, "_")
        .trim('_')
}