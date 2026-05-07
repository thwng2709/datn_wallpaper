package com.itsthwng.twallpaper.ui.component.zipper.zipperlock

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.core.graphics.drawable.toDrawable
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityZipLockMainBinding
import com.itsthwng.twallpaper.databinding.PinCodeLayoutBinding
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.local.LocalData
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.component.permission.OverlayPermissionActivity
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.Glszl_GameAdapter
import com.itsthwng.twallpaper.ui.component.zipper.lib.Glszl_GameAdapters.ZipperCompatActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.ChooseZipperActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_AppAdapter
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_CheckBoxUpdater
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.PrefKey
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_JobUtil
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_LiveService
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_SharedPreferencisUtil
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_Uscreen
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.Glszl_UserDataAdapter
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.MoreSettingsActivity
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.ui.ZipLockPersonalisation
import com.itsthwng.twallpaper.ui.component.zipper.zipperlock.utils.StatusBarUtils
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import java.util.Objects

class ZipLockMainActivity : ZipperCompatActivity() {
    private lateinit var dataBinding: ActivityZipLockMainBinding
    private lateinit var packagePrefs: SharedPreferences
    private lateinit var pref: Glszl_SharedPreferencisUtil
    private var pinIsActive: Boolean = false
    var isActivated: Boolean = false

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage: LocalStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(AppConfig.updateResources(newBase, localStorage.langCode))
        } else {
            super.attachBaseContext(null)
        }
    }

    private fun setButtonListeners() {
        dataBinding.customizeBtn.setOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_CUSTOMIZE)
            startNewActivity(ZipLockPersonalisation::class.java)
        }

        dataBinding.backBtn.setOnClickListener { finish() }

        dataBinding.settingsBtn.setOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_SETTING)
            startNewActivity(MoreSettingsActivity::class.java)
        }

        // Start the "Set Now" flow
        dataBinding.setLockScreenBtn.setOnClickListener {
            AppConfig.logEventTracking(Constants.EventKey.GO_TO_ZIPPER_FOREGROUND)

            startSetNowFlow()
            val bundle = Bundle()
            bundle.putInt("type", 0)
            bundle.putBoolean("next", true)
            startNewActivity(ChooseZipperActivity::class.java, bundle)
        }
    }

    override fun onResume() {
        super.onResume()

        // Check if we should clear temp values
        if (shouldClearTempValues()) {
            clearAllTempValues()
        }

        // Check quyền overlay và cập nhật UI
        val hasOverlayPermission = checkPermissionOverlay(false)
        val currentlyActivated = Glszl_UserDataAdapter.LoadPref(PrefKey.ACTIVE, this) == 1

        if (currentlyActivated && !hasOverlayPermission) {
            // Disable zipper and PIN
            Glszl_CheckBoxUpdater.toggleAndPersistState(true, PrefKey.ACTIVE, this, true)
            Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "2")
            saveLock("lock_screen", 2)

            // Stop services
            Glszl_LiveService.StopService(this)

            // Update UI
            isActivated = false
            updateLockScreenSwitchState()
            dataBinding.lockPinSwitch.setChecked(false)
        } else if (!currentlyActivated && hasOverlayPermission && dataBinding.lockScreenSwitch.isChecked) {
            Glszl_CheckBoxUpdater.toggleAndPersistState(false, PrefKey.ACTIVE, this, true)
            Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "1")
            saveLock("lock_screen", 1)
            Glszl_LiveService.StartServiceIfNotNull(this)
            Glszl_JobUtil.scheduleJob(this)
            isActivated = true
            updateLockScreenSwitchState()
        } else if (currentlyActivated != isActivated) {
            isActivated = currentlyActivated
            updateLockScreenSwitchState()
        }
    }

    private fun shouldClearTempValues(): Boolean {
        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
        val flowActive = prefs.getBoolean(PREF_SET_NOW_FLOW_ACTIVE, false)

        if (!flowActive) {
            // No active flow, should clear
            return true
        }

        // Check if flow has timed out
        val flowStartTime = prefs.getLong(PREF_SET_NOW_FLOW_TIMESTAMP, 0)
        val currentTime = System.currentTimeMillis()

        if (currentTime - flowStartTime > FLOW_TIMEOUT_MILLIS) {
            // Flow has timed out, clear flow state and temp values
            prefs.edit {
                putBoolean(PREF_SET_NOW_FLOW_ACTIVE, false)
                    .putLong(PREF_SET_NOW_FLOW_TIMESTAMP, 0)
            }
            return true
        }

        // Flow is still active and within timeout
        return false
    }

    private fun clearAllTempValues() {
        Glszl_AppAdapter.SaveWallpaperBgTemp(this, -1)
        Glszl_AppAdapter.SaveWallpaperTemp(this, -1)
        Glszl_AppAdapter.SaveZipperTemp(this, -1)
        Glszl_AppAdapter.SaveChainTemp(this, -1)
        Glszl_AppAdapter.SaveFontTemp(this, -1)
    }

    private fun startSetNowFlow() {
        val prefs = getSharedPreferences(packageName, MODE_PRIVATE)
        prefs.edit {
            putBoolean(PREF_SET_NOW_FLOW_ACTIVE, true)
                .putLong(PREF_SET_NOW_FLOW_TIMESTAMP, System.currentTimeMillis())
        }
    }

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        dataBinding = ActivityZipLockMainBinding.inflate(layoutInflater)
        setContentView(dataBinding.root)

        this.pref = Glszl_SharedPreferencisUtil(this)
        pinIsActive = this.pref.getPinIsActive()
        setButtonListeners()

        StatusBarUtils.setStatusBarColor(this)

        Glszl_Uscreen.Init(this)
        val applicationContext = getApplicationContext()
        this.packagePrefs =
            applicationContext.getSharedPreferences(applicationContext.packageName, 0)

        try {
            (getSystemService(KEYGUARD_SERVICE) as KeyguardManager).newKeyguardLock("IN")
                .disableKeyguard()
        } catch (_: Exception) {
        }
        if (Glszl_UserDataAdapter.LoadPref(PrefKey.ACTIVE, this) == 1) {
            Glszl_LiveService.StartServiceIfNotNull(this)
        }
        window.decorView.systemUiVisibility = 0

        if (!this.isActivated) {
            Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "2")
            saveLock("lock_screen", 2)
            dataBinding.lockScreenSwitch.setChecked(false)
            dataBinding.lockScreenStatus.setText(R.string.of)
        } else {
            saveLock("lock_screen", 1)
            Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "1")
            dataBinding.lockScreenSwitch.setChecked(true)
            dataBinding.lockScreenStatus.setText(R.string.on)
        }
        val pinIsActive = pref.getPinIsActive()
        dataBinding.lockPinSwitch.setChecked(pinIsActive)
        if (pinIsActive) {
            dataBinding.lockPinStatus.text = getResources().getString(R.string.on)
            dataBinding.lockPinTitle.text = getResources().getString(R.string.disable_pin_lock)
        } else {
            dataBinding.lockPinStatus.text = getResources().getString(R.string.of)
            dataBinding.lockPinTitle.text = getResources().getString(R.string.enable_pin_lock)
        }
        dataBinding.lockPinSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { _: CompoundButton?, value: Boolean ->
            pref.setPinIsActive(value)
            if (value) {
                dataBinding.lockPinStatus.text = getResources().getString(R.string.on)
                dataBinding.lockPinTitle.text = getResources().getString(R.string.disable_pin_lock)
                this.enterPin()
                return@OnCheckedChangeListener
            }
            dataBinding.lockPinStatus.text = getResources().getString(R.string.of)
            dataBinding.lockPinTitle.text = getResources().getString(R.string.enable_pin_lock)
        })
        if (this.isActivated) {
            dataBinding.lockScreenStatus.setText(R.string.on)
            Glszl_LiveService.StartServiceIfNotNull(this)
        } else {
            dataBinding.lockScreenStatus.setText(R.string.of)
        }
        dataBinding.lockScreenSwitch.setOnCheckedChangeListener(createSwitchListener())
        startNotificationAlarm()
    }

    @SuppressLint("ResourceType")
    private fun enterPin() {
        val strArr = arrayOf<String?>(
            getString(R.string.question1), getString(R.string.question2), getString(
                R.string.question3
            ), getString(R.string.question4)
        )
        val pinBinding = PinCodeLayoutBinding.inflate(LayoutInflater.from(this))
        val create = AlertDialog.Builder(this).create()
        Objects.requireNonNull<Window>(create.window).setBackgroundDrawable(
            Color.TRANSPARENT.toDrawable()
        )
        create.setCancelable(true)
        create.setView(pinBinding.root)
        pinBinding.pinView.setImeAction(EditorInfo.IME_ACTION_NEXT)
        pinBinding.reenterPinView.setImeAction(EditorInfo.IME_ACTION_NEXT)
        if (pref.getPin() != null) {
            pinBinding.pinView.setText(pref.getPin())
            pinBinding.reenterPinView.setText(pref.getPin())
        }
        // Force enable security question - always required
        pref.setSecurityQuestionIsActive(true)
        pinBinding.securitySwitch.setChecked(true)
        pinBinding.securitySwitch.visibility = View.GONE  // Hide the toggle switch
        pinBinding.spinnerQuestions.visibility = View.VISIBLE
        pinBinding.questionAnswer.visibility = View.VISIBLE
        if (pref.getSecurityQuestion().isNotEmpty()) {
            pinBinding.questionAnswer.setText(pref.getSecurityQuestion())
            pinBinding.spinnerQuestions.setSelection(pref.getSequrityQIndex())
        }
        @SuppressLint("ResourceType") val arrayAdapter: ArrayAdapter<*> =
            ArrayAdapter<Any?>(this, 17367048, strArr)
        arrayAdapter.setDropDownViewResource(17367049)
        pinBinding.spinnerQuestions.adapter = arrayAdapter
        // Listener still exists but won't be called since switch is hidden
        pinBinding.securitySwitch.setOnCheckedChangeListener { _: CompoundButton?, value: Boolean ->
            pref.setSecurityQuestionIsActive(value)
        }

        pinBinding.cancelPinBtn.setSafeOnClickListener(800) {
            pinIsActive = false
            pref.setPinIsActive(false)
            dataBinding.lockPinSwitch.setChecked(false)
            create.cancel()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            val v = currentFocus
            val token =
                if (v != null) v.windowToken else window.decorView.windowToken
            if (imm != null && token != null) {
                imm.hideSoftInputFromWindow(token, 0) // chỉ ẩn nếu đang hiển thị
            }
        }
        pinBinding.savePinBtn.setSafeOnClickListener(800) {
            val firstEnteredPin = pinBinding.pinView.text.toString()
            val secondeEnteredPin = pinBinding.reenterPinView.text.toString()
            if (firstEnteredPin.length == 4 && secondeEnteredPin.length == 4 && firstEnteredPin == secondeEnteredPin) {
                pinIsActive = true
                pref.setPin(firstEnteredPin)
                if (pinBinding.securitySwitch.isChecked) {
                    pref.setSequrityQIndex(pinBinding.spinnerQuestions.selectedItemPosition)
                    pref.setSecurityQuestion(
                        pinBinding.questionAnswer.text.toString()
                    )
                }
                create.cancel()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
                val v = currentFocus
                val token = if (v != null) v.windowToken else window.decorView.windowToken
                if (imm != null && token != null) {
                    imm.hideSoftInputFromWindow(token, 0) // chỉ ẩn nếu đang hiển thị
                }
                showToast(getString(R.string.enable_pin_lock_success))
                return@setSafeOnClickListener
            }
            showToast(getString(R.string.pin_is_incorrect))
        }
        create.setOnDismissListener {
            dataBinding.lockPinSwitch.setChecked(
                pinIsActive
            )
        }
        val decorView = create.window!!.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        create.show()
    }

    private fun startNotificationAlarm() {
        getSharedPreferences(
            PrefKey.DATA_FILENAME,
            0
        ).getBoolean("notification_shown", false)
    }

    public override fun onActivityResult(i: Int, i2: Int, intent: Intent?) {
        super.onActivityResult(i, i2, intent)

        if (i == OverlayPermissionActivity.REQUEST_OVERLAY_PERMISSION) {
            if (i2 == RESULT_OK && intent != null) {
                val permissionGranted = intent.getBooleanExtra(
                    OverlayPermissionActivity.EXTRA_PERMISSION_GRANTED, false
                )
                if (permissionGranted) {
                    AppConfig.logEventTracking(Constants.EventKey.GRANT_OVERLAY_PERMISSION)

                    // Persist activation so onResume won't flip it back
                    Glszl_CheckBoxUpdater.toggleAndPersistState(
                        false,
                        PrefKey.ACTIVE,
                        this,
                        true
                    )

                    // Update UI and services
                    isActivated = true
                    Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "1")
                    saveLock("lock_screen", 1)
                    Glszl_LiveService.StartServiceIfNotNull(this)
                    Glszl_JobUtil.scheduleJob(this)
                    dataBinding.lockScreenStatus.setText(R.string.on)

                    // Force switch ON without triggering listener side effects
                    dataBinding.lockScreenSwitch.setOnCheckedChangeListener(null)
                    dataBinding.lockScreenSwitch.setChecked(true)
                    dataBinding.lockScreenSwitch.setOnCheckedChangeListener(createSwitchListener())
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.DENY_OVERLAY_PERMISSION)

                    // Permission not granted, reset switch
                    dataBinding.lockScreenSwitch.setChecked(false)
                    showToast(getString(R.string.overlay_permission_denied_message))
                }
            } else if (i2 == RESULT_CANCELED) {
                // User clicked "Not Now" or back, reset switch
                dataBinding.lockScreenSwitch.setChecked(false)
                showToast(getString(R.string.overlay_permission_denied_message))
            }
        }
    }

    fun saveLock(str: String?, num: Int) {
        packagePrefs.edit {
            putInt(str, num)
        }
    }

    fun saveRunning(z: Boolean) {
        packagePrefs.edit { putBoolean("lockRunning", z) }
    }

    fun startNewActivity(cls: Class<*>?, bundle: Bundle = Bundle()) {
        val activityOptionsCompat = ActivityOptionsCompat.makeCustomAnimation(
            this,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        )
        val intent = Intent(this, cls)
        intent.putExtras(bundle)
        mainToIntent = intent
        startActivity(intent, activityOptionsCompat.toBundle())
    }

    private fun createSwitchListener(): CompoundButton.OnCheckedChangeListener {
        return CompoundButton.OnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            saveRunning(isChecked)
            if (isChecked) {
                // When enabling, check permission first
                if (checkPermissionOverlay(true)) {
                    // Permission already granted, enable lock screen
                    val str = PrefKey.ACTIVE
                    isActivated = true
                    Glszl_CheckBoxUpdater.toggleAndPersistState(false, str, this, true)
                    Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "1")
                    saveLock("lock_screen", 1)
                    AppConfig.logEventTracking(Constants.EventKey.ENABLE_ZIPPER)

                    Glszl_LiveService.StartServiceIfNotNull(this)
                    Glszl_JobUtil.scheduleJob(this)
                    dataBinding.lockScreenStatus.setText(R.string.on)
                    showToast(getString(R.string.enable_zip_locker_toast))
                } else {
                    // Permission not granted, will be handled in onActivityResult
                    // Keep switch in pending state
                }
            } else {
                // When disabling, no permission check needed
                isActivated = false
                val str = PrefKey.ACTIVE
                Glszl_CheckBoxUpdater.toggleAndPersistState(true, str, this, true)

                Glszl_AppAdapter.SetLock(Glszl_GameAdapter.ctx, "2")
                saveLock("lock_screen", 2)
                Glszl_LiveService.StopService(this)
                dataBinding.lockScreenStatus.setText(R.string.of)
                showToast(getString(R.string.disable_zip_locker_toast))
            }
        }
    }

    private fun updateLockScreenSwitchState() {
        if (isActivated) {
            dataBinding.lockScreenSwitch.setOnCheckedChangeListener(null) // Temporarily remove listener
            dataBinding.lockScreenSwitch.setChecked(true)
            dataBinding.lockScreenSwitch.setOnCheckedChangeListener(createSwitchListener()) // Re-add listener
            dataBinding.lockScreenStatus.setText(R.string.on)

            Glszl_LiveService.StartServiceIfNotNull(this)
        } else {
            dataBinding.lockScreenSwitch.setOnCheckedChangeListener(null) // Temporarily remove listener
            dataBinding.lockScreenSwitch.setChecked(false)
            dataBinding.lockScreenSwitch.setOnCheckedChangeListener(createSwitchListener()) // Re-add listener
            dataBinding.lockScreenStatus.setText(R.string.of)

        }
    }

    @SuppressLint("ResourceType")
    fun checkPermissionOverlay(requestPermissionNeeded: Boolean): Boolean {
        try {
            if (Settings.canDrawOverlays(this)) {
                return true
            }
            if (requestPermissionNeeded) {
                // Navigate to overlay permission screen and wait for result
                OverlayPermissionActivity.startForResult(this, true)
            }
            return false
        } catch (_: Exception) {
            return true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        var mainToIntent: Intent? = null
        private const val PREF_SET_NOW_FLOW_ACTIVE = "set_now_flow_active"
        private const val PREF_SET_NOW_FLOW_TIMESTAMP = "set_now_flow_timestamp"
        private const val FLOW_TIMEOUT_MILLIS = (30 * 60 * 1000).toLong() // 30 minutes

        @JvmStatic
        fun endSetNowFlow(context: Context) {
            val prefs = context.getSharedPreferences(context.packageName, MODE_PRIVATE)
            prefs.edit {
                putBoolean(PREF_SET_NOW_FLOW_ACTIVE, false)
                    .putLong(PREF_SET_NOW_FLOW_TIMESTAMP, 0)
            }
        }
    }
}