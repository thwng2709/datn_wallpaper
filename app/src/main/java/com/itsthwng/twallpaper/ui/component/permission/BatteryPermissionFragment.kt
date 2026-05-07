package com.itsthwng.twallpaper.ui.component.permission

import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentBatteryPermissionBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BatteryPermissionFragment : BaseFragmentBinding<FragmentBatteryPermissionBinding>() {

    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _: ActivityResult ->
        try {
            if(findNavControllerSafety()?.currentDestination?.id == R.id.batteryPermissionFragment){
                goToMainActivity()
            }
        } catch (ex : Exception) { }
    }

    override fun getContentViewId() = R.layout.fragment_battery_permission

    override fun initializeViews() {
        if(localStorage.isFirstOpenBatteryPermission){
            localStorage.isFirstOpenBatteryPermission = false
            AppConfig.logEventTracking(Constants.EventKey.BATTERYPER_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.BATTERYPER_OPEN_2ND)
        }
    }

    override fun registerListeners() {
        dataBinding.btnNext.setOnClickListener {
            requestPermission()
        }
        dataBinding.btnNotNow.setOnClickListener {
            if(!AppConfig.isDoubleClick()){
                if(findNavControllerSafety()?.currentDestination?.id == R.id.batteryPermissionFragment) {
                    goToMainActivity()
                }
            }
        }
    }

    override fun initializeData() {}

    private fun goToMainActivity(){
        val intent = Intent(requireActivity(), MainActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(
            requireActivity(),
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent, options.toBundle())
        requireActivity().finish()
    }

    private fun requestPermission() {
        val intentSettings = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        with(intentSettings) {
            context?.let {
                data = Uri.fromParts("package", it.packageName, null)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
        }
        startForResult.launch(intentSettings)
    }
}