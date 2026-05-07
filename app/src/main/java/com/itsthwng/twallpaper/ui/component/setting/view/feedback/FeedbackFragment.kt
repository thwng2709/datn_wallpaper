package com.itsthwng.twallpaper.ui.component.setting.view.feedback

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.text.Editable
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentFeedbackBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import com.itsthwng.twallpaper.utils.AppConfig.logEventTracking
import com.itsthwng.twallpaper.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FeedbackFragment :
    BaseFragmentBinding<FragmentFeedbackBinding>() {
    private val selectedImages = mutableSetOf<Uri>()
    private val maxImageCount = 3
    private lateinit var adapter: SelectedImagesAdapter

    private val pickImages = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            selectedImages.add(it)
            updateImageCount()
        }
    }

    override fun getContentViewId() = R.layout.fragment_feedback

    @SuppressLint("ClickableViewAccessibility")
    override fun initializeViews() {
        if (localStorage.isFirstOpenFeedbackFragment) {
            logEventTracking(Constants.EventKey.FEEDBACK_OPEN_1ST)
            localStorage.isFirstOpenFeedbackFragment = false
        } else {
            logEventTracking(Constants.EventKey.FEEDBACK_OPEN_2ND)
        }
        adapter = SelectedImagesAdapter(selectedImages.toMutableList()) { uri ->
            selectedImages.remove(uri)
            updateImageCount()
        }
        dataBinding.rvImage.adapter = adapter
        
        // Hide keyboard when tap outside EditText
        dataBinding.root.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                activity?.currentFocus?.let { currentFocus ->
                    if (currentFocus is EditText) {
                    val outRect = android.graphics.Rect()
                    currentFocus.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        currentFocus.clearFocus()
                        hideKeyboard(currentFocus)
                    }
                    }
                }
            }
            false
        }
        
        // Hide keyboard when touching or scrolling
        dataBinding.scrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Check if touch is outside EditText
                    activity?.currentFocus?.let { currentFocus ->
                        if (currentFocus is EditText) {
                            val outRect = Rect()
                            currentFocus.getGlobalVisibleRect(outRect)
                            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                                currentFocus.clearFocus()
                                hideKeyboard(currentFocus)
                            }
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    // Hide keyboard when scrolling
                    val currentFocus = activity?.currentFocus
                    if (currentFocus is EditText) {
                        currentFocus.clearFocus()
                        hideKeyboard(currentFocus)
                    }
                }
            }
            false
        }
        
        // Set red asterisk for Content
        val contentText = "* ${getString(R.string.content)}"
        val contentSpannable = SpannableString(contentText)
        contentSpannable.setSpan(
            ForegroundColorSpan(Color.RED),
            0, 1, // Only color the asterisk
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        dataBinding.txtContent.text = contentSpannable

        // Set up character limit for content
        val maxLength = 2000
        dataBinding.edtContent.filters = arrayOf(InputFilter.LengthFilter(maxLength))

        // Set up character count listener
        dataBinding.edtContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val currentLength = s?.length ?: 0
                dataBinding.tvCharCount.text = "$currentLength/$maxLength"
                
                // Auto scroll to show cursor when typing
                dataBinding.edtContent.post {
                    val cursorLine = dataBinding.edtContent.layout?.getLineForOffset(dataBinding.edtContent.selectionEnd) ?: 0
                    val cursorBottom = dataBinding.edtContent.layout?.getLineBottom(cursorLine) ?: 0
                    
                    val scrollViewHeight = dataBinding.scrollView.height
                    val currentScrollY = dataBinding.scrollView.scrollY
                    val edtContentTop = dataBinding.edtContent.top
                    
                    // Check if cursor is below visible area
                    if (edtContentTop + cursorBottom > currentScrollY + scrollViewHeight - 300) { // 300px for keyboard
                        dataBinding.scrollView.smoothScrollTo(0, edtContentTop + cursorBottom - scrollViewHeight + 300)
                    }
                }
            }
        })

        dataBinding.edtContent.setOnTouchListener(View.OnTouchListener { view, event ->
            if (view.isFocusable && event.action == MotionEvent.ACTION_DOWN) {
                view.parent.requestDisallowInterceptTouchEvent(true)
            } else if (event.action == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        })

        // Auto scroll when EditText is focused to prevent keyboard overlap
        dataBinding.edtContent.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dataBinding.root.postDelayed({
                    dataBinding.scrollView.smoothScrollTo(0, dataBinding.edtContent.bottom)
                }, 300)
            }
        }

        // Set up image selection
        dataBinding.layoutSelectImage.setOnClickListener {
            // 2. Gọi launch với một PickVisualMediaRequest để chỉ định loại media
            pickImages.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    override fun onDestroyView() {
        // Hide keyboard when fragment is destroyed
        dataBinding.root.let { hideKeyboard(it) }
        super.onDestroyView()
    }

    private fun updateImageCount() {
        dataBinding.tvPhotoCount.text = "${selectedImages.size}/$maxImageCount"
        if(selectedImages.size == 3){
            dataBinding.layoutSelectImage.isEnabled = false
        } else {
            dataBinding.layoutSelectImage.isEnabled = true
        }
        showSelectedImages()
    }

    private fun showSelectedImages() {
        adapter.addImages(selectedImages.toMutableList(), maxCount = 3)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun registerListeners() {
        dataBinding.btnBack.setOnClickListener {
            findNavControllerSafety()?.navigateUp()
        }

        // Set up single selection behavior for problem types
        val problemTypeCheckBoxes = listOf(
            dataBinding.txtSuggestions,
            dataBinding.txtBugs,
            dataBinding.txtOthers
        )

        problemTypeCheckBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck all other checkboxes
                    problemTypeCheckBoxes.forEach { otherCheckBox ->
                        if (otherCheckBox != checkBox) {
                            otherCheckBox.isChecked = false
                        }
                    }
                }
            }
        }

        dataBinding.btnSubmit.setOnClickListener {
            val content = dataBinding.edtContent.text?.toString()?.trim() ?: ""

            // Validate content format
            if (content.isEmpty()) {
                showToast(R.string.not_empty_content)
                return@setOnClickListener
            }

            // Get selected problem type (only one can be selected now)
            val problemType = when {
                dataBinding.txtSuggestions.isChecked -> getString(R.string.suggestions)
                dataBinding.txtBugs.isChecked -> getString(R.string.bugs)
                dataBinding.txtOthers.isChecked -> getString(R.string.others)
                else -> getString(R.string.suggestions) // Default to suggestions if none selected
            }

            // Send feedback via mailto only
            val attachmentInfo = if (selectedImages.isNotEmpty()) {
                "\nAttachments selected in app: ${selectedImages.size}"
            } else {
                ""
            }
            val subject = Uri.encode(Constants.SUBJECT_EMAIL)
            val body = Uri.encode("Problem type: $problemType\nContent: $content$attachmentInfo")
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${Constants.EMAIL}?subject=$subject&body=$body")
            }

            val mailApps = emailIntent.resolveActivity(requireActivity().packageManager)
            if (mailApps != null) {
                startActivity(
                    Intent.createChooser(
                        emailIntent,
                        getString(R.string.choose_email)
                    )
                )
            } else {
                showToast(R.string.sent_feedback_fail)
            }
        }
    }

    override fun initializeData() {

    }
}