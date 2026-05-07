package com.itsthwng.twallpaper.utils.permission

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.utils.PermissionManager
import javax.inject.Inject

/**
 * Handles permission requests with automatic rationale and settings dialogs
 * Can be used in Activities and Fragments
 */
class PermissionHandler @Inject constructor(
    private val dialogManager: PermissionDialogManager
) {
    
    @Volatile
    private var isRequestInProgress = false
    
    private var permissionLauncher: ActivityResultLauncher<String>? = null
    private var multiplePermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var pendingPermissionCallback: PermissionCallback? = null
    
    /**
     * Initialize permission handler in an Activity
     */
    fun init(activity: AppCompatActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            handlePermissionResult(activity, isGranted)
        }
        
        multiplePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            handlePermissionResult(activity, allGranted)
        }
    }
    
    /**
     * Initialize permission handler in a Fragment
     */
    fun init(fragment: Fragment) {
        permissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            fragment.activity?.let { activity ->
                handlePermissionResult(activity, isGranted)
            }
        }
        
        multiplePermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            fragment.activity?.let { activity ->
                handlePermissionResult(activity, allGranted)
            }
        }
    }
    
    /**
     * Request storage permission with automatic handling
     */
    fun requestStoragePermission(
        activity: Activity,
        callback: PermissionCallback
    ) {
        // Prevent multiple simultaneous permission requests
        if (isRequestInProgress) {
            // Show feedback to user that a permission request is already in progress
            android.widget.Toast.makeText(
                activity,
                "Permission request already in progress. Please wait.",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        
        isRequestInProgress = true
        pendingPermissionCallback = callback
        
        // Check if permission is already granted
        if (PermissionManager.hasStoragePermission(activity)) {
            isRequestInProgress = false
            pendingPermissionCallback = null
            callback.onGranted()
            return
        }
        
        // Get the required permission
        val permission = PermissionManager.getStoragePermission()
        if (permission == null) {
            // No permission needed (Android Q+)
            isRequestInProgress = false
            pendingPermissionCallback = null
            callback.onGranted()
            return
        }
        
        // Check if we should show rationale
        if (PermissionManager.shouldShowStoragePermissionRationale(activity)) {
            dialogManager.showPermissionExplanationDialog(
                activity = activity,
                onGrantClick = {
                    requestPermission(permission)
                },
                onCancelClick = {
                    isRequestInProgress = false
                    pendingPermissionCallback = null
                    callback.onDenied(showedRationale = true)
                }
            )
        } else {
            // Request permission directly
            requestPermission(permission)
        }
    }
    
    /**
     * Request media read permission (for Android 13+)
     */
    fun requestMediaReadPermission(
        activity: Activity,
        callback: PermissionCallback
    ) {
        // Prevent multiple simultaneous permission requests
        if (isRequestInProgress) {
            callback.onDenied(showedRationale = false)
            return
        }
        
        isRequestInProgress = true
        pendingPermissionCallback = callback
        
        // Check if permission is already granted
        if (PermissionManager.hasMediaReadPermission(activity)) {
            callback.onGranted()
            return
        }
        
        // Get the required permissions
        val permissions = PermissionManager.getMediaReadPermissions()
        if (permissions.isEmpty()) {
            // No permission needed
            callback.onGranted()
            return
        }
        
        // Check if we should show rationale
        if (PermissionManager.shouldShowMediaReadPermissionRationale(activity)) {
            dialogManager.showPermissionExplanationDialog(
                activity = activity,
                message = activity.getString(R.string.media_read_permission_explanation),
                onGrantClick = {
                    requestMultiplePermissions(permissions)
                },
                onCancelClick = {
                    callback.onDenied(showedRationale = true)
                }
            )
        } else {
            // Request permissions directly
            requestMultiplePermissions(permissions)
        }
    }
    
    private fun requestPermission(permission: String) {
        permissionLauncher?.launch(permission)
    }
    
    private fun requestMultiplePermissions(permissions: Array<String>) {
        multiplePermissionLauncher?.launch(permissions)
    }
    
    private fun handlePermissionResult(activity: Activity, isGranted: Boolean) {
        val callback = pendingPermissionCallback ?: return
        
        // Clear the callback reference immediately to avoid memory leak
        pendingPermissionCallback = null
        isRequestInProgress = false
        
        if (isGranted) {
            callback.onGranted()
        } else {
            // Check if user selected "Don't ask again"
            val permission = PermissionManager.getStoragePermission()
            val shouldShowRationale = permission?.let {
                activity.shouldShowRequestPermissionRationale(it)
            } ?: false
            
            if (!shouldShowRationale) {
                // User denied with "Don't ask again"
                dialogManager.showGoToSettingsDialog(
                    activity = activity,
                    onSettingsClick = {
                        callback.onPermanentlyDenied()
                        // Clean up dialog reference after callback
                        dialogManager.dismissCurrentDialog()
                    },
                    onCancelClick = {
                        callback.onDenied(showedRationale = false)
                        // Clean up dialog reference after callback
                        dialogManager.dismissCurrentDialog()
                    }
                )
            } else {
                // User denied but can ask again
                callback.onDenied(showedRationale = false)
            }
        }
    }
    
    /**
     * Check if permission was revoked while app is running
     * Call this in onResume() to detect permission changes
     */
    fun checkPermissionStatus(
        activity: Activity,
        onPermissionRevoked: () -> Unit
    ) {
        // Check if we previously had permission but it was revoked
        if (!PermissionManager.hasStoragePermission(activity)) {
            dialogManager.dismissCurrentDialog()
            onPermissionRevoked()
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        dialogManager.dismissCurrentDialog()
        pendingPermissionCallback = null
        permissionLauncher = null
        multiplePermissionLauncher = null
        isRequestInProgress = false
    }
}

/**
 * Permission request callback
 */
interface PermissionCallback {
    fun onGranted()
    fun onDenied(showedRationale: Boolean)
    fun onPermanentlyDenied() = onDenied(false)
}