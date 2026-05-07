package com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityMoreSettingsBinding
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants

class MoreSettingsActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityMoreSettingsBinding
    var isBatteryActive: Boolean = false
    var isDateActive: Boolean = false
    var isSoundActive: Boolean = false
    var isTimeActive: Boolean = false
    var isVibrationActive: Boolean = false

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(AppConfig.updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityMoreSettingsBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        StatusBarUtils.setStatusBarColor(this)
        initializeViews()
        setListeners()
    }

    private fun initializeViews() {
        // Initialize sound setting
        val soundActive =
            Glszl_CheckBoxUpdater.isStateEnabled(PrefKey.SOUND_ACTIVE, this)
        isSoundActive = soundActive
        dataBinding.switchSound.setChecked(soundActive)
        setStatus(dataBinding.soundStatus, this.isSoundActive)

        // Initialize background setting
        val enableBackground = Glszl_AppAdapter.isShowBackground(this)
        setStatus(dataBinding.statusEnableBackground, enableBackground)
        dataBinding.switchEnableBackground.setChecked(enableBackground)

        // Initialize vibration setting
        val vibrationActive =
            Glszl_CheckBoxUpdater.isStateEnabled(PrefKey.VIBRATION_ACTIVE, this)
        this.isVibrationActive = vibrationActive
        dataBinding.switchVibration.setChecked(vibrationActive)
        setStatus(dataBinding.statusVibration, isVibrationActive)

        // Initialize date setting
        val dateActive = Glszl_CheckBoxUpdater.isStateEnabled(PrefKey.DATE_ACTIVE, this)
        this.isDateActive = dateActive
        dataBinding.switchDate.setChecked(dateActive)
        setStatus(dataBinding.tvDateStatus, this.isDateActive)

        // Initialize time setting
        val timeActive = Glszl_CheckBoxUpdater.isStateEnabled(PrefKey.TIME_ACTIVE, this)
        this.isTimeActive = timeActive
        dataBinding.switchTime.setChecked(timeActive)
        setStatus(dataBinding.tvTimeStatus, this.isTimeActive)

        // Initialize battery setting
        val batteryActive =
            Glszl_CheckBoxUpdater.isStateEnabled(PrefKey.BATTERY_ACTIVE, this)
        this.isBatteryActive = batteryActive
        dataBinding.switchBattery.setChecked(batteryActive)
        setStatus(dataBinding.tvBatteryStatus, isBatteryActive)
    }

    private fun setListeners() {
        dataBinding.backBtn.setOnClickListener { finish() }

        dataBinding.switchSound.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            val z2 = isSoundActive
            val str = PrefKey.SOUND_ACTIVE
            isSoundActive = Glszl_CheckBoxUpdater.toggleAndPersistState(z2, str, this, true)
            setStatus(dataBinding.soundStatus, isSoundActive)
        }

        dataBinding.switchEnableBackground.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            Glszl_AppAdapter.setIsShowBackground(this, isChecked)
            if (isChecked) {
                dataBinding.statusEnableBackground.text = getResources().getString(R.string.on)
            } else {
                dataBinding.statusEnableBackground.text = getResources().getString(R.string.of)
            }
        }

        dataBinding.switchVibration.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            val z2 = isVibrationActive
            val str = PrefKey.VIBRATION_ACTIVE
            isVibrationActive =
                Glszl_CheckBoxUpdater.toggleAndPersistState(
                    z2,
                    str,
                    this,
                    true
                )
            setStatus(dataBinding.statusVibration, isVibrationActive)
        }

        dataBinding.switchDate.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            val z2 = isDateActive
            val str = PrefKey.DATE_ACTIVE
            val moreSettings2 = this
            isDateActive = Glszl_CheckBoxUpdater.toggleAndPersistState(z2, str, moreSettings2, true)
            setStatus(dataBinding.tvDateStatus, isDateActive)
        }

        dataBinding.switchTime.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            val z2 = isTimeActive
            val str = PrefKey.TIME_ACTIVE
            isTimeActive = Glszl_CheckBoxUpdater.toggleAndPersistState(
                z2,
                str,
                this,
                true
            )
            setStatus(dataBinding.tvTimeStatus, isTimeActive)
        }

        dataBinding.switchBattery.setOnCheckedChangeListener { _: CompoundButton?, _: Boolean ->
            val z2 = isBatteryActive
            val str = PrefKey.BATTERY_ACTIVE
            isBatteryActive =
                Glszl_CheckBoxUpdater.toggleAndPersistState(
                    z2,
                    str,
                    this,
                    true
                )
            setStatus(dataBinding.tvBatteryStatus, isBatteryActive)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OverlayPermissionActivity.REQUEST_OVERLAY_PERMISSION) {
            if (resultCode == RESULT_OK && data != null) {
                val permissionGranted = data.getBooleanExtra(
                    OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, false
                )
                if (!permissionGranted) {
                    AppConfig.logEventTracking(Constants.EventKey.DENY_OVERLAY_PERMISSION)
                    showToast(getString(R.string.overlay_permission_denied_message))
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.GRANT_OVERLAY_PERMISSION)
                }
            } else if (resultCode == RESULT_CANCELED) {
                // User clicked "Not Now" or back
                showToast(getString(R.string.overlay_permission_denied_message))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun setStatus(textView: TextView, value: Boolean) {
        if (value) {
            textView.text = getResources().getString(R.string.on)
        } else {
            textView.text = getResources().getString(R.string.of)
        }
    }
}
