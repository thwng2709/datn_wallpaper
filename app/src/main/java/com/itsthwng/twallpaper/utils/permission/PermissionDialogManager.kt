package com.itsthwng.twallpaper.utils.permission

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.itsthwng.twallpaper.R
import javax.inject.Inject

/**
 * Manages permission-related dialogs
 * Provides consistent UI for permission explanations and denials
 */
class PermissionDialogManager @Inject constructor() {
    
    private var currentDialog: AlertDialog? = null
    
    /**
     * Show explanation dialog for why permission is needed
     */
    fun showPermissionExplanationDialog(
        activity: Activity,
        message: String? = null,
        onGrantClick: () -> Unit,
        onCancelClick: () -> Unit = {}
    ) {
        dismissCurrentDialog()
        
        currentDialog = AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required))
            .setMessage(message ?: activity.getString(R.string.storage_permission_explanation))
            .setPositiveButton(activity.getString(R.string.grant_permission)) { dialog, _ ->
                dialog.dismiss()
                onGrantClick()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onCancelClick()
            }
            .setOnCancelListener {
                onCancelClick()
            }
            .create()
            
        currentDialog?.show()
    }
    
    /**
     * Show dialog when permission is permanently denied
     * Guides user to app settings
     */
    fun showGoToSettingsDialog(
        activity: Activity,
        message: String? = null,
        onSettingsClick: () -> Unit = { openAppSettings(activity) },
        onCancelClick: () -> Unit = {}
    ) {
        dismissCurrentDialog()
        
        currentDialog = AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.permission_required))
            .setMessage(message ?: activity.getString(R.string.permission_denied_go_to_settings))
            .setPositiveButton(activity.getString(R.string.go_to_settings)) { dialog, _ ->
                dialog.dismiss()
                onSettingsClick()
            }
            .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                onCancelClick()
            }
            .setOnCancelListener {
                onCancelClick()
            }
            .create()
            
        currentDialog?.show()
    }
    
    /**
     * Dismiss any currently showing dialog
     */
    fun dismissCurrentDialog() {
        currentDialog?.dismiss()
        currentDialog = null
    }
    
    /**
     * Open app settings page
     */
    private fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }
    
    /**
     * Check if a dialog is currently showing
     */
    fun isDialogShowing(): Boolean = currentDialog?.isShowing == true
}