package com.itsthwng.twallpaper.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.itsthwng.twallpaper.App
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.utils.AppConfig
import javax.inject.Inject

abstract class BaseDialogFragment : DialogFragment() {
    
    @Inject
    lateinit var localStorage: LocalStorage
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Update resources when dialog is attached
        updateLanguageResources(context)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Update resources before inflating view
        context?.let { updateLanguageResources(it) }
        return super.onCreateView(inflater, container, savedInstanceState)
    }
    
    override fun onResume() {
        super.onResume()
        // Update resources when dialog resumes
        context?.let { updateLanguageResources(it) }
    }
    
    private fun updateLanguageResources(context: Context) {
        try {
            // Try to use injected localStorage if available
            if (::localStorage.isInitialized) {
                AppConfig.updateResources(context, localStorage.langCode)
            } else {
                // Fallback to App instance
                val appLocalStorage = App.instance.localStorage
                AppConfig.updateResources(context, appLocalStorage.langCode)
            }
        } catch (e: Exception) {
            // If all fails, try to get from context
            e.printStackTrace()
        }
    }
}