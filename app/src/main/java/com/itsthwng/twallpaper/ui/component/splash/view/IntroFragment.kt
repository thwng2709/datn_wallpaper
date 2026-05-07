package com.itsthwng.twallpaper.ui.component.splash.view

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentIntroBinding
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.splash.viewmodel.TopicViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.CommonUtil
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Constants.PERMISSIONS
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.workManager.LocaleSyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : BaseViewModelFragmentBinding<FragmentIntroBinding, TopicViewModel>() {

    private lateinit var mViewPager: ViewPager2
    private var countItemIntro = 0
    override fun getContentViewId() = R.layout.fragment_intro

    override fun onResume() {
        super.onResume()
        (activity as SplashLoadingActivity).checkShowRequestInternet()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            enqueueLocaleSync(it, localStorage.langCode)
        }
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

    override fun initializeViews() {
        if (localStorage.langCode == "ar") {
            dataBinding.txtNext.rotation = 180f
        }
        mViewPager = dataBinding.viewPager
        mViewPager.adapter =
            IntroductionViewPageAdapter(this, initIntroductionData(requireContext()))
        dataBinding.indicatorPageOnboarding.attachTo(mViewPager)
        mViewPager.offscreenPageLimit = 4
        mViewPager.currentItem = 0
    }

    private fun getItem(): Int {
        return mViewPager.currentItem
    }

    private fun initIntroductionData(context: Context): List<IntroductionItem> {
        val title1 = context.getString(R.string.intro1_msg)
        val description1 = context.getString(R.string.text_intro1_msg)
        val imageResource1 = 1
        val introductionItem1 = IntroductionItem(title1, description1, imageResource1)

        val title2 = context.getString(R.string.intro2_msg)
        val description2 = context.getString(R.string.text_intro2_msg)
        val imageResource2 = 2
        val introductionItem2 = IntroductionItem(title2, description2, imageResource2)

        val title3 = context.getString(R.string.intro3_msg)
        val description3 = context.getString(R.string.text_intro3_msg)
        val imageResource3 = 3
        val introductionItem3 = IntroductionItem(title3, description3, imageResource3)

        val title4 = context.getString(R.string.intro4_msg)
        val description4 = context.getString(R.string.text_intro3_msg)
        val imageResource4 = 4
        val introductionItem4 = IntroductionItem(title4, description4, imageResource4)
        val list =
            listOf(introductionItem1, introductionItem2, introductionItem3, introductionItem4)
        countItemIntro = list.size
        return list
    }

    override fun registerListeners() {
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    3 -> {
                        dataBinding.bottomLayout.visibility = View.VISIBLE
                        dataBinding.txtNext.visibility = View.VISIBLE
                        dataBinding.txtNext.text = resources.getString(R.string.get_started)
                    }

                    else -> {
                        dataBinding.bottomLayout.visibility = View.VISIBLE
                        dataBinding.txtNext.visibility = View.VISIBLE
                        dataBinding.txtNext.text = resources.getString(R.string.text_next)
                    }
                }
            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
            override fun onPageScrollStateChanged(arg0: Int) {}
        })

        dataBinding.txtNext.setOnClickListener {
            val countIntro = if (countItemIntro == 0) 4 else countItemIntro
            if (getItem() > countIntro - 2) {
                goToMainActivity()
            } else {
                mViewPager.setCurrentItem(getItem() + 1, true)
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun hasManageFilePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            CommonUtil.hasPermissions(
                permissions = PERMISSIONS,
                activity = requireActivity()
            )
        }
    }

    private fun goToMainActivity() {
        try {
            if (activity != null) {
                try {
                    AppConfig.logEventTracking(Constants.EventKey.GO_TO_HOME)
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.putExtra("source_screen", TAG)
                var options: ActivityOptions? = null
                context?.let {
                    options = ActivityOptions.makeCustomAnimation(
                        it,
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                }
                if (options != null) {
                    startActivity(intent, options.toBundle())
                } else {
                    startActivity(intent)
                }
                requireActivity().finish()
            }
        } catch (e: Exception) {
            Logger.e(e.message)
        }
    }

    override fun initializeData() {
    }

    companion object {
        const val TAG = "IntroFragment"
    }
}