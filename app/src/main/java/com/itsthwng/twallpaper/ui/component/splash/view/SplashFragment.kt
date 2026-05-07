package com.itsthwng.twallpaper.ui.component.splash.view

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.AppDatabase
import com.itsthwng.twallpaper.data.CommonInfo
import com.itsthwng.twallpaper.data.dao.CategoryDao
import com.itsthwng.twallpaper.databinding.FragmentSplashBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.workManager.LocaleSyncWorker
import com.itsthwng.twallpaper.workManager.OrderConf
import com.itsthwng.twallpaper.workManager.normalizeId
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : BaseFragmentBinding<FragmentSplashBinding>() {
    @Inject
    lateinit var db: AppDatabase
    @Inject
    lateinit var categoryDao: CategoryDao
    private val gson = Gson()

    private var timeoutJob: Job? = null
    private var isLoadingComplete = false
    private var isTimeoutReached = false
    private var isNavigationDispatched = false
    private var pendingNavigateOnResume = false

    override fun getContentViewId() = R.layout.fragment_splash
    override fun initializeViews() {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun enqueueLocaleSync(context: Context, locale: String) {
        val req = OneTimeWorkRequestBuilder<LocaleSyncWorker>()
            .setInputData(workDataOf("locale" to locale))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST) // nhanh cho lần đầu
            .addTag("LOCALE_SYNC")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "LOCALE_SYNC",
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("HienDV_Debug_Ads : SplashFragment onViewCreated")
        startSplashTimeoutFallback()

        if (localStorage.isFirstInstall) {
            localStorage.isFirstInstall = false
            localStorage.firstTimeOpenApp = System.currentTimeMillis()
        }
        App.instance.doWhenInitialized {
            val ctx = context
            if (ctx == null) {
                onLoadingCompleted("context_null")
                return@doWhenInitialized
            }

            if ((localStorage.currentVersionDataR2 == 0L && !localStorage.isFirstOpen)
                || (localStorage.currentVersionDataR2 != 0L && CommonInfo.current_version_data_cloudflare_R2 != 0L
                        && CommonInfo.current_version_data_cloudflare_R2 > localStorage.currentVersionDataR2)
            ) {
                enqueueLocaleSync(ctx, localStorage.langCode)
                onLoadingCompleted("locale_sync_enqueued")
            } else if (localStorage.currentVersionDataR2 != 0L) {
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        val orderJson = CommonInfo.categoryOrderJson // file JSON thứ tự + aliases
                        val orderConf = gson.fromJson(orderJson, OrderConf::class.java)
                        println("Get Order Conf: ${orderConf ?: 0}")

                        db.withTransaction {
                            val categorySort = applyOrderByContainment(categoryDao, orderConf)
                            localStorage.categorySort = categorySort.joinToString(",")
                        }
                    } finally {
                        withContext(Dispatchers.Main) {
                            onLoadingCompleted("category_order_applied")
                        }
                    }
                }
            } else {
                onLoadingCompleted("no_sync_needed")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (pendingNavigateOnResume) {
            pendingNavigateOnResume = false
            tryNavigateNext("resume_retry")
        }
    }

    override fun onDestroyView() {
        timeoutJob?.cancel()
        timeoutJob = null
        super.onDestroyView()
    }

    private fun startSplashTimeoutFallback() {
        timeoutJob?.cancel()
        timeoutJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(Constants.TIME_DELAY_SPLASH_MAX)
            isTimeoutReached = true
            Logger.d("Splash timeout reached, trying to continue")
            tryNavigateNext("timeout")
        }
    }

    private fun onLoadingCompleted(reason: String) {
        if (isLoadingComplete) return
        isLoadingComplete = true
        Logger.d("Splash loading completed: $reason")
        tryNavigateNext("load_completed")
    }

    private fun tryNavigateNext(trigger: String) {
        if (isNavigationDispatched) return
        if (!isLoadingComplete && !isTimeoutReached) return

        if (!lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
            pendingNavigateOnResume = true
            Logger.d("Defer splash navigation until resumed, trigger=$trigger")
            return
        }

        isNavigationDispatched = true
        timeoutJob?.cancel()
        timeoutJob = null
        handleWhenLoadInterDone()
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
                if (affected == 0) {
                    println("⚠️ UPDATE missed (unexpected): $chosenDbId")
                }
            } else {
                println("❌ No DB match for canonical='${conf.canonicalId}', aliases=${conf.aliases}")
            }
        }

        val leftovers = dbIds.filter { it !in usedDbIds }
        leftovers.forEach { leftoverId ->
            val affected = categoryDao.updateOnePosition(leftoverId, pos++)
            resultOrder += leftoverId               // <-- bổ sung phần còn lại
            if (affected == 0) println("⚠️ LEFTOVER not updated: $leftoverId")
        }

        println("✅ Positions applied. Total set = $pos")
        return resultOrder
    }


    override fun registerListeners() {
    }

    override fun initializeData() {
    }

    //----------------------------------------------------------------------------------------------

    private fun goToHomeActivity() {
        if (activity != null) {
            try {
                AppConfig.logEventTracking(Constants.EventKey.GO_TO_HOME)
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            var options: ActivityOptions? = null
            context?.let {
                options = ActivityOptions.makeCustomAnimation(
                    it,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
            }
            try {
                if (options != null) {
                    requireActivity().startActivity(intent, options.toBundle())
                } else {
                    requireActivity().startActivity(intent)
                }
            } catch (_: Exception) {
                requireActivity().startActivity(intent)
            }
            requireActivity().finish()
        }
    }

    private fun goToLanguageFragment() {
        try {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_LANGUAGE_1)
        } catch (e: Exception) {
            Logger.e("LogEventTracking error: ${e.message}")
        }
        localStorage.isFirstOpen = true
        try {
            findNavControllerSafety()?.navigate(R.id.atcOpenAskAgeScreen)
        } catch (e: Throwable) {
            Logger.e(e.message)
        }
    }

    private fun handleWhenLoadInterDone() {
        if (!localStorage.isFirstOpen) {
            if ((CommonInfo.show_on_boarding_kill_app && !App.instance.hasHandledColdStart) || localStorage.callOnboardingFlow) {
                localStorage.callOnboardingFlow = false
                App.instance.hasHandledColdStart = true
                goToLanguageFragment()
            } else {
                goToHomeActivity()
            }
        } else {
            goToLanguageFragment()
        }
    }

    companion object {
        const val TAG = "SplashFragment"
    }
}