package com.itsthwng.twallpaper.ui.component.search

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.itsthwng.twallpaper.extension.setSafeOnClickListener
import com.itsthwng.twallpaper.extension.toArrayList
import com.itsthwng.twallpaper.local.LocalStorage
import com.itsthwng.twallpaper.ui.base.BaseActivityBinding
import com.itsthwng.twallpaper.ui.component.search.viewModel.SearchViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Global
import com.itsthwng.twallpaper.utils.Logger
import com.itsthwng.twallpaper.utils.NetworkUtils
import com.itsthwng.twallpaper.local.LocalData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.graphics.drawable.toDrawable
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.ActivitySearchBinding
import com.itsthwng.twallpaper.databinding.ItemFilterPopupBinding

@AndroidEntryPoint
class SearchActivity : BaseActivityBinding<ActivitySearchBinding, SearchViewModel>() {

    @Inject
    lateinit var localStorage: LocalStorage

    private lateinit var dialogFilter: Dialog
    var linearLayoutManager: LinearLayoutManager? = null
    var isLoading = false

    override fun attachBaseContext(newBase: Context?) {
        if (newBase != null) {
            val localStorage = LocalData(newBase, "sharedPreferences")
            super.attachBaseContext(
                AppConfig.updateResources(newBase, localStorage.langCode)
            )
        } else {
            super.attachBaseContext(null)
        }
    }

    override fun getContentViewId() = R.layout.activity_search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding.model = viewModel
        val networkCollected = NetworkUtils.isNetworkConnected()
        if (networkCollected) {
            dataBinding.noInternetLayout.visibility = View.GONE
        } else {
            dataBinding.noInternetLayout.visibility = View.VISIBLE
        }
        initView()
        initListeners()
        initObservers()
        logTracking()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun initView() {
        // Adapter + fav list ban đầu
        viewModel.wallpaperAdapter.favList =
            Global.convertStringToLis(localStorage.favourites).toArrayList()
        viewModel.wallpaperAdapter.onFavClick = { viewModel.updateWallpaper(it) }

        // RecyclerView
        dataBinding.rvImageByCategory.adapter = viewModel.wallpaperAdapter

        // UI trạng thái mặc định
        dataBinding.tvSearchWord.visibility = View.GONE
        dataBinding.tvNoData.visibility = View.GONE
        dataBinding.progressLoadMore.visibility = View.GONE
        dataBinding.sihmmer.visibility = View.GONE
    }

    private fun initListeners() {
        // 3) Nút Refresh: thử lại ngay
        dataBinding.btnRetry.setOnClickListener {
            val networkCollected = NetworkUtils.isNetworkConnected()
            if(networkCollected){
                dataBinding.noInternetLayout.visibility = View.GONE
            } else {
                dataBinding.noInternetLayout.visibility = View.VISIBLE
            }
        }

        // 4) Nút Go to Settings: mở cài đặt mạng
        dataBinding.btnSetting.setOnClickListener {
            try {
                AppConfig.logEventTracking(Constants.HOME_REQUIRE_INTERNET_YES)
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Logger.e(e.message)
            }
        }
        // Scroll -> load trang tiếp
        dataBinding.rvImageByCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (viewModel.wallpaperAdapter.itemCount - 1 === linearLayoutManager?.findLastVisibleItemPosition() && !isLoading) {

                        viewModel.loadNextPage()

                    }
                }
            }
        })

        // Filter dialog already handled below with hideKeyboard

        // Back
        dataBinding.btnBack.setSafeOnClickListener {
            handleWhenLoadInterBackDone()
        }

        // Set up input filter for special characters
        val specialCharFilter = android.text.InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                // Allow letters, digits, space, and Vietnamese characters
                if (!Character.isLetterOrDigit(char) && char != ' ' && !isVietnameseChar(char)) {
                    return@InputFilter ""
                }
            }
            null
        }
        
        dataBinding.etSearch.filters = arrayOf(specialCharFilter)
        
        // Handle search action on keyboard, and search text
        dataBinding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // Hide keyboard when user press search
                hideKeyboard()
                viewModel.onQueryChanged(dataBinding.etSearch.text.toString())
                true
            } else {
                false
            }
        }
        
        // Hide keyboard when tap outside EditText
        dataBinding.root.setOnTouchListener { view, event ->
            if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = android.graphics.Rect()
                    v.getGlobalVisibleRect(outRect)
                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        // Clicked outside EditText
                        v.clearFocus()
                        hideKeyboard()
                    }
                }
            }
            false // Return false to not consume the touch event
        }
        
        // Hide keyboard when scroll
        dataBinding.rvImageByCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })
        
        // Hide keyboard when click on items
        dataBinding.btnFilter.setOnClickListener {
            hideKeyboard()
            showFilterDialogue()
        }
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 1) Items: append/update vào adapter
                launch {
                    viewModel.items.collect { list ->
                        if(NetworkUtils.isNetworkConnected()){
                            dataBinding.noInternetLayout.visibility = View.GONE
                        } else {
                            dataBinding.noInternetLayout.visibility = View.VISIBLE
                        }
                        val current = viewModel.wallpaperAdapter.itemCount
                        if (list.isEmpty()) {
                            if (!viewModel.isInitialLoading.value) {
                                dataBinding.tvNoData.visibility =
                                    if (viewModel.searchWord.value.isBlank()) View.VISIBLE else View.VISIBLE
                            }
                            // clear hiển thị (tuỳ bạn)
                            viewModel.wallpaperAdapter.clear()
                        } else {
                            dataBinding.tvNoData.visibility = View.GONE
                            if (current == 0) {
                                viewModel.wallpaperAdapter.updateData(list.toMutableList())
                            } else if (list.size > current) {
                                val delta = list.subList(current, list.size)
                                viewModel.wallpaperAdapter.loadMore(delta.toMutableList())
                            } else {
                                // giữ nguyên
                            }
                        }
                    }
                }

                // 2) Loading flags
                launch {
                    viewModel.isInitialLoading.collect { show ->
                        dataBinding.sihmmer.visibility = if (show) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.isLoadMore.collect { show ->
                        dataBinding.progressLoadMore.visibility = if (show) View.VISIBLE else View.GONE
                    }
                }

                // 3) Cập nhật caption theo query
                launch {
                    viewModel.searchWord.collect { q ->
                        if (q.isBlank()) {
                            dataBinding.tvSearchWord.visibility = View.GONE
                            dataBinding.tvNoDataFor.text = getString(R.string.please_type_something)
                        } else {
                            dataBinding.tvSearchWord.visibility = View.VISIBLE
                            dataBinding.tvSearchWord.text = "\"$q\""
                            dataBinding.tvNoDataFor.text = getString(R.string.no_data_for)
                        }
                    }
                }
                
                // Update filter chip based on selected filter
                launch {
                    viewModel.filterType.collect { filterType ->
                        updateFilterChip(filterType)
                    }
                }

                // 4) Favorites → cập nhật localStorage + adapter favList
                launch {
                    viewModel.favoriteWalls.collect { likedList ->
                        if (likedList.isEmpty()) return@collect
                        val ids = likedList.mapNotNull { it.id }
                        localStorage.favourites =
                            Global.listOfIntegerToString(likedList.filter { it.id != null }
                                .map { it.id!! }) ?: ""
                        viewModel.wallpaperAdapter.refreshData(
                            Global.convertStringToLis(
                                localStorage.favourites
                            )
                        )
                    }
                }

            }
        }
    }

    private fun showFilterDialogue() {
        // Clear focus and hide keyboard FIRST with a small delay for smooth animation
        dataBinding.etSearch.clearFocus()
        hideKeyboard()
        
        // Small delay to let keyboard animation complete
        dataBinding.root.postDelayed({
            dialogFilter = Dialog(this).apply {
                window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                // Prevent dialog from showing keyboard
                window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            }
            val view = LayoutInflater.from(this).inflate(R.layout.item_filter_popup, null, false)
            val binding: ItemFilterPopupBinding = DataBindingUtil.bind(view)!!

            // Get current filter type and update UI to show selection
            val currentFilter = viewModel.filterType.value
            updateFilterDialogSelection(binding, currentFilter)

            binding.btnAll.setOnClickListener {
                viewModel.onFilterChanged(2)
                dialogFilter.dismiss()
                // Keep focus cleared to prevent keyboard
                dataBinding.etSearch.clearFocus()
            }
            binding.btnStatic.setOnClickListener {
                viewModel.onFilterChanged(0)
                dialogFilter.dismiss()
                // Keep focus cleared to prevent keyboard
                dataBinding.etSearch.clearFocus()
            }
            binding.btnLive.setOnClickListener {
                viewModel.onFilterChanged(1)
                dialogFilter.dismiss()
                // Keep focus cleared to prevent keyboard
                dataBinding.etSearch.clearFocus()
            }

            dialogFilter.setContentView(view)
            dialogFilter.setCancelable(true)
            
            // Handle dialog dismiss to keep keyboard hidden
            dialogFilter.setOnDismissListener {
                dataBinding.etSearch.clearFocus()
            }
            
            dialogFilter.show()
        }, 100) // 100ms delay for smooth transition
    }

    private fun refreshFavList() {
        viewModel.wallpaperAdapter.refreshData(
            Global.convertStringToLis(localStorage.favourites)
        )
    }

    override fun onResume() {
        val networkCollected = NetworkUtils.isNetworkConnected()
        if (networkCollected) {
            dataBinding.noInternetLayout.visibility = View.GONE
        } else {
            dataBinding.noInternetLayout.visibility = View.VISIBLE
        }
        refreshFavList()
        
        // Only hide keyboard if EditText doesn't have focus
        // This prevents hiding keyboard when user just tapped the search field
        if (!dataBinding.etSearch.hasFocus()) {
            hideKeyboard()
        }
        
        super.onResume()
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(dataBinding.etSearch.windowToken, 0)
    }
    
    private fun isVietnameseChar(char: Char): Boolean {
        val vietnameseChars = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴÈÉẸẺẼÊỀẾỆỂỄÌÍỊỈĨÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠÙÚỤỦŨƯỪỨỰỬỮỲÝỴỶỸĐ"
        return vietnameseChars.contains(char)
    }
    
    private fun updateFilterChip(filterType: Int) {
        // Always hide the chip filter
        dataBinding.chipActiveFilter.visibility = View.GONE
        
        when (filterType) {
            0 -> { // Static
                // Change filter icon to purple
                dataBinding.btnFilter.setColorFilter(ContextCompat.getColor(this, R.color.color_theme_purple_light))
            }
            1 -> { // Live
                // Change filter icon to purple
                dataBinding.btnFilter.setColorFilter(ContextCompat.getColor(this, R.color.color_theme_purple_light))
            }
            2 -> { // All
                // Reset filter icon color
                dataBinding.btnFilter.clearColorFilter()
            }
        }
    }
    
    private fun updateFilterDialogSelection(binding: ItemFilterPopupBinding, filterType: Int) {
        // Reset all selections first
        binding.btnStatic.foreground = null
        binding.btnLive.foreground = null
        binding.btnAll.foreground = null
        
        // Apply selection based on current filter
        when (filterType) {
            0 -> { // Static selected - add white border
                binding.btnStatic.foreground = ContextCompat.getDrawable(this, R.drawable.bg_filter_selected)
            }
            1 -> { // Live selected - add white border
                binding.btnLive.foreground = ContextCompat.getDrawable(this, R.drawable.bg_filter_selected)
            }
            2 -> { // All selected - add white border (same as others)
                binding.btnAll.foreground = ContextCompat.getDrawable(this, R.drawable.bg_filter_selected)
            }
        }
    }

    private fun handleWhenLoadInterBackDone() {
        finish()
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenSearchScreen) {
            localStorage.isFirstOpenSearchScreen = false
            AppConfig.logEventTracking(Constants.EventKey.SEARCH_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.SEARCH_OPEN_2ND)
        }
    }

    // Các hàm của BaseActivityBinding (không dùng ở đây)
    override fun initializeViews() {}
    override fun registerListeners() {}
    override fun initializeData() {}
    
    companion object {
        private const val SEARCH_TAG = "SearchActivity"
    }
}