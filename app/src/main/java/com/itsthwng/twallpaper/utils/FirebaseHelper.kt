package com.itsthwng.twallpaper.utils

import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itsthwng.twallpaper.data.model.CategoriesImages
import com.itsthwng.twallpaper.data.model.HomeData
import com.itsthwng.twallpaper.data.model.SettingData
import kotlinx.coroutines.tasks.await

object FirebaseHelper {

    /** Dung lượng tối đa đọc 1 file JSON (tuỳ chỉnh nếu cần). */
    @JvmStatic
    var maxJsonBytes: Long = 2L * 1024 * 1024 // 2MB

    /** Cung cấp langCode. Mặc định đọc từ localStorage.langCode của bạn. */
    @JvmStatic
    var langCodeProvider: () -> String = {
        // TODO: thay bằng localStorage.langCode của bạn
        try {
            @Suppress("ClassName")
            val cls = Class.forName("com.yourapp.data.local.localStorage")
            val field = cls.getDeclaredField("langCode")
            field.isAccessible = true
            (field.get(null) as? String) ?: "en"
        } catch (_: Throwable) {
            "en"
        }
    }

    /** Cho phép inject FirebaseStorage (dễ test/multiple buckets). */
    @JvmStatic
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

    val gson = Gson()

    /** Helper generic: tải JSON từ Storage và parse về kiểu T. */
    @JvmStatic
    suspend inline fun <reified T> fetchJsonFromStorage(path: String): T {
        val ref = storage.reference.child(path)
        val bytes = ref.getBytes(maxJsonBytes).await()
        val json = bytes.toString(Charsets.UTF_8)
        val type = object : TypeToken<T>() {}.type
        return gson.fromJson(json, type)
    }

    /** Chuẩn hoá lang; chỉ cho "vi" / "en". */
    @JvmStatic
    fun safeLang(lang: String?): String =
        when (lang?.lowercase()) { "vi", "en" -> lang.lowercase() ; else -> "en" }

    // ----------------- Categories -----------------

    /** Nếu categories.json là MẢNG: [ {..}, {..} ] */
    @JvmStatic
    suspend fun getCategories(lang: String? = null): List<SettingData.CategoriesItem> {
        val l = safeLang(lang ?: langCodeProvider())
        val path = "$l/categories.json"
        return fetchJsonFromStorage<List<SettingData.CategoriesItem>>(path)
    }

    @JvmStatic
    suspend fun getHomeData(lang: String? = null): HomeData {
        val l = safeLang(lang ?: langCodeProvider())
        val path = "$l/homeData/homeData.json"
        return fetchJsonFromStorage<HomeData>(path)
    }
    // ----------------- Images by Category -----------------

    /**
     * Lấy danh sách images theo category folder (vd: "animals" hoặc "landscape").
     * Mặc định tìm "images.json" trong folder đó.
     */
    @JvmStatic
    suspend fun getImagesByCategory(
        categoryFolder: String,
        lang: String? = null,
        fileName: String = "images.json"
    ): CategoriesImages {
        val l = safeLang(lang ?: langCodeProvider())
        val cat = categoryFolder.trim().trim('/')
        val path = "$l/$cat/$fileName"
        return fetchJsonFromStorage<CategoriesImages>(path)
    }

    private fun userPath(id: String) = "coins/v1/users/$id.json"

}