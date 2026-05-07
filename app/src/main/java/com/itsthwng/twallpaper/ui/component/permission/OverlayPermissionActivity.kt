package com.itsthwng.twallpaper.ui.component.permission

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivityOverlayPermissionBinding
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.utils.AppConfig
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OverlayPermissionActivity : AppCompatActivity() {
    
    @Inject
    lateinit var localStorage: LocalStorage
    
    private lateinit var binding: ActivityOverlayPermissionBinding
    private lateinit var navController: NavController
    
    override fun attachBaseContext(newBase: Context?) {
        // Get localStorage from application context since DI is not ready yet
        val appContext = newBase?.applicationContext
        val localStorage = if (appContext != null) {
            try {
                App.instance.localStorage
            } catch (e: Exception) {
                null
            }
        } else null
        
        val context = if (newBase != null && localStorage != null) {
            AppConfig.updateResources(newBase, localStorage.langCode)
        } else {
            newBase
        }
        super.attachBaseContext(context)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOverlayPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Navigate directly to overlay permission fragment
        navController.navigate(R.id.overlayPermissionFragment)
    }
    
    companion object {
        const val REQUEST_OVERLAY_PERMISSION = 1001
        const val EXTRA_PERMISSION_GRANTED = "permission_granted"
        
        fun start(context: Context, fromZipperLock: Boolean = true) {
            val intent = Intent(context, OverlayPermissionActivity::class.java)
            intent.putExtra("from_zipper_lock", fromZipperLock)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
        
        fun startForResult(activity: android.app.Activity, fromZipperLock: Boolean = true) {
            val intent = Intent(activity, OverlayPermissionActivity::class.java)
            intent.putExtra("from_zipper_lock", fromZipperLock)
            activity.startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }
}