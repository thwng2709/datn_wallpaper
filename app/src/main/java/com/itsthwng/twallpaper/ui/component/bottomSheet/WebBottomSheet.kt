package com.itsthwng.twallpaper.ui.component.bottomSheet

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.BottomsheetWebBinding
import com.itsthwng.twallpaper.ui.base.BaseBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import com.itsthwng.twallpaper.utils.Constants

@AndroidEntryPoint
class WebBottomSheet(var type: Int) : BaseBottomSheetDialogFragment() {
    lateinit var binding: BottomsheetWebBinding
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var v: View? = null


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog1: DialogInterface ->
            val dialog = dialog1 as BottomSheetDialog
            dialog.setCanceledOnTouchOutside(false)
            val bottomSheet =
                dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
                BottomSheetBehavior.from(bottomSheet).skipCollapsed = true
                BottomSheetBehavior.from(bottomSheet).isHideable = true
            }
        }
        return bottomSheetDialog
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(activity).inflate(R.layout.bottomsheet_web, container, false)
        binding = DataBindingUtil.bind(v!!)!!
        binding.root.isNestedScrollingEnabled = true
        binding.webView.settings.javaScriptEnabled = true
        if (type == 1) {
            binding.tvHeading.text = requireActivity().getString(R.string.privacy_policy)
            binding.webView.loadUrl(Constants.PRIVACY_URL)
        }
        if (type == 2) {
            binding.tvHeading.text = requireActivity().getString(R.string.terms_of_use)
            binding.webView.loadUrl(Constants.TERMS_URL)
        }
        binding.btnClose.setOnClickListener { dismiss() }
        return binding.root
    }

}