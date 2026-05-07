package com.itsthwng.twallpaper.ui.component.history.fragment

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.data.model.SettingData
import com.itsthwng.twallpaper.data.model.isZipper
import com.itsthwng.twallpaper.databinding.FragmentHistoryBinding
import com.itsthwng.twallpaper.ui.base.BaseViewModelFragmentBinding
import com.itsthwng.twallpaper.ui.component.MainActivity
import com.itsthwng.twallpaper.ui.component.history.model.EmptyActionType
import com.itsthwng.twallpaper.ui.component.history.model.EmptyStateConfig
import com.itsthwng.twallpaper.ui.component.history.model.EmptyStateMessage
import com.itsthwng.twallpaper.ui.component.history.viewmodel.HistoryViewModel
import com.itsthwng.twallpaper.utils.AppConfig
import com.itsthwng.twallpaper.utils.Constants
import com.itsthwng.twallpaper.utils.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment :
    BaseViewModelFragmentBinding<FragmentHistoryBinding, HistoryViewModel>() {

    private var deletedWallpaper: SettingData.WallpapersItem? = null
    private var deleteSnackbar: Snackbar? = null
    private var undoJob: Job? = null

    override fun getContentViewId(): Int {
        return R.layout.fragment_history
    }

    override fun initializeViews() {
        println("HistoryFragment: initializeViews called")
        initObservers()
        setupFilterChips()
        setupClickListeners()
        setupSwipeRefresh()
        setupEntranceAnimation()
        logTracking()
        setupWallpaperAdapter()
        dataBinding.model = viewModel

        // Force check initial state and set default empty state
        if (viewModel.historyWalls.value.isEmpty()) {
            println("HistoryFragment: Initial state is empty, showing empty state")
            showEmptyState()
            // Also set default empty state content
            updateEmptyState(EmptyStateMessage.NeverSetWallpaper)
        }
    }

    private fun initObservers() {
        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    if (isLoading) {
                        showShimmerLoading()
                    } else {
                        hideShimmerLoading()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.historyWalls.collect { historyList ->
                    println("HistoryFragment: historyList size = ${historyList.size}")
                    if (historyList.isEmpty()) {
                        println("HistoryFragment: Showing empty state")
                        showEmptyState()
                        // Ensure empty state content is set
                        val currentEmptyType = viewModel.emptyStateType.value
                        if (currentEmptyType != null) {
                            updateEmptyState(currentEmptyType)
                        } else {
                            // Default to NeverSetWallpaper if no type is set yet
                            updateEmptyState(EmptyStateMessage.NeverSetWallpaper)
                        }
                    } else {
                        dataBinding.loutNoHistory.visibility = View.GONE
                        dataBinding.rvHistory.visibility = View.VISIBLE
                        viewModel.wallpaperAdapter.updateData(historyList)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filterType.collect { filterType ->
                    updateFilterChipsState(filterType)
                }
            }
        }

        // Removed selection mode observers - no longer needed

        // Observe empty state type
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.emptyStateType.collect { emptyStateType ->
                    println("HistoryFragment: emptyStateType = $emptyStateType")
                    emptyStateType?.let {
                        println("HistoryFragment: Updating empty state UI")
                        updateEmptyState(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favoriteWalls.collect { favourite ->
                    val favIds = favourite.mapNotNull { it.id }
                    viewModel.wallpaperAdapter.refreshData(favIds)
                }
            }
        }
    }

    private fun setupFilterChips() {
        dataBinding.chipAll.setOnClickListener {
            try {
                val isFirstTimeClickAllHistory = localStorage.isFirstClickAllFilterHistory
                if (isFirstTimeClickAllHistory) {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_ALL_FILTER_1ST)
                    localStorage.isFirstClickAllFilterHistory = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_ALL_FILTER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.setFilterType(HistoryViewModel.FilterType.ALL)
        }
        dataBinding.chipHome.setOnClickListener {
            try {
                val isFirstTimeClickHomeScreenHistory = localStorage.isFirstClickHomeFilterHistory
                if (isFirstTimeClickHomeScreenHistory) {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_HOME_FILTER_1ST)
                    localStorage.isFirstClickHomeFilterHistory = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_HOME_FILTER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.setFilterType(HistoryViewModel.FilterType.HOME)
        }
        dataBinding.chipLock.setOnClickListener {
            try {
                val isFirstTimeClickLockScreenHistory =
                    localStorage.isFirstClickLockScreenFilterHistory
                if (isFirstTimeClickLockScreenHistory) {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_LOCK_FILTER_1ST)
                    localStorage.isFirstClickLockScreenFilterHistory = false
                } else {
                    AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_LOCK_FILTER_2ND)
                }
            } catch (e: Exception) {
                Logger.e("LogEventTracking error: ${e.message}")
            }
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.setFilterType(HistoryViewModel.FilterType.LOCK)
        }
    }

    private fun updateFilterChipsState(filterType: HistoryViewModel.FilterType) {
        // Animate filter changes
        val chips = listOf(dataBinding.chipAll, dataBinding.chipHome, dataBinding.chipLock)
        val selectedChip = when (filterType) {
            HistoryViewModel.FilterType.ALL -> dataBinding.chipAll
            HistoryViewModel.FilterType.HOME -> dataBinding.chipHome
            HistoryViewModel.FilterType.LOCK -> dataBinding.chipLock
        }

        chips.forEach { chip ->
            if (chip == selectedChip) {
                // Animate selected chip
                chip.animate()
                    .scaleX(1.08f)
                    .scaleY(1.08f)
                    .setDuration(100)
                    .withEndAction {
                        chip.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()

                // Set selected state with ripple
                chip.setBackgroundResource(R.drawable.ripple_chip_selected)
                chip.elevation = 4f
                chip.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            } else {
                // Set unselected state with ripple
                chip.setBackgroundResource(R.drawable.ripple_chip)
                chip.elevation = 0f
                chip.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        requireContext(),
                        R.color.color_text_primary
                    )
                )

                // Subtle scale down animation
                chip.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        chip.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                    }
                    .start()
            }
        }

        // Animate RecyclerView content change with fade
        dataBinding.rvHistory.animate()
            .alpha(0.6f)
            .setDuration(150)
            .withEndAction {
                dataBinding.rvHistory.animate()
                    .alpha(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    private fun setupClickListeners() {
        dataBinding.btnBack.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        dataBinding.btnSearch.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            toggleSearchView()
        }

        // Menu button shows options - temporarily disabled
        // dataBinding.btnMenu.setOnClickListener {
        //     it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
        //     showHistoryMenu(it)
        // }

        // Removed selection mode button listeners - no longer needed

        dataBinding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
        })

        dataBinding.searchEditText.setOnEditorActionListener { _, _, _ ->
            hideKeyboard()
            true
        }

        // Empty state action button
        dataBinding.cardEmptyAction.setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            handleEmptyAction()
        }
    }

    private fun toggleSearchView() {
        if (dataBinding.searchEditText.isVisible) {
            dataBinding.searchEditText.visibility = View.GONE
            dataBinding.searchEditText.setText("")
            hideKeyboard()
        } else {
            dataBinding.searchEditText.visibility = View.VISIBLE
            dataBinding.searchEditText.requestFocus()
            showKeyboard()
        }
    }

    private fun showKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(dataBinding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(dataBinding.searchEditText.windowToken, 0)
    }

    private fun setupSwipeRefresh() {
        var startY = 0f
        var isDragging = false

        dataBinding.swipeRefresh.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    isDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - startY
                    if (dy > 24f && !dataBinding.rvHistory.canScrollVertically(-1)) {
                        isDragging = true
                        dataBinding.swipeRefresh.translationY = dy / 3f
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (isDragging) {
                        dataBinding.swipeRefresh.animate()
                            .translationY(0f)
                            .setDuration(180)
                            .start()
                        viewModel.loadHistoryWallpapers()
                    }
                    isDragging = false
                }
            }
            true
        }
    }

    override fun registerListeners() {
        // No additional listeners needed
    }

    override fun initializeData() {
        // No additional data initialization needed
    }

    override fun onResume() {
        super.onResume()
        // History wallpapers are observed automatically via Flow
    }

    private fun setupEntranceAnimation() {
        // Animate header
        val rootLayout = dataBinding.root as LinearLayout
        val header = rootLayout.getChildAt(0)
        header.alpha = 0f
        header.translationY = -50f
        header.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate search edit text
        dataBinding.searchEditText.alpha = 0f
        dataBinding.searchEditText.translationY = -30f
        dataBinding.searchEditText.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setStartDelay(100)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate filter chips
        val chipsContainer = dataBinding.chipAll.parent as View
        chipsContainer.alpha = 0f
        chipsContainer.translationX = -100f
        chipsContainer.animate()
            .alpha(1f)
            .translationX(0f)
            .setDuration(400)
            .setStartDelay(200)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()

        // Animate content with stagger effect
        dataBinding.swipeRefresh.alpha = 0f
        dataBinding.swipeRefresh.scaleX = 0.9f
        dataBinding.swipeRefresh.scaleY = 0.9f
        dataBinding.swipeRefresh.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(300)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    private fun setupWallpaperAdapter() {
        // Handle favorite click
        viewModel.wallpaperAdapter.onFavClick = { wallpaper ->
            viewModel.toggleFavorite(wallpaper)
        }

        // Handle delete click - DO NOT delete when clicking favorite
        viewModel.wallpaperAdapter.onDeleteClick = { wallpaper ->
            handleDeleteFromHistory(wallpaper)
        }

        // Long press hint removed - no selection mode
    }

    private fun handleDeleteFromHistory(
        wallpaper: SettingData.WallpapersItem,
        position: Int = -1
    ) {
        // Cancel any previous undo job
        undoJob?.cancel()
        deleteSnackbar?.dismiss()

        // Store the deleted wallpaper
        deletedWallpaper = wallpaper

        // Remove from history
        val isZipper = wallpaper.isZipper()
        viewModel.removeFromHistory(wallpaper.id ?: return, isZipper)

        // Show Snackbar with undo action
        deleteSnackbar = Snackbar.make(
            dataBinding.root,
            getString(R.string.removed_from_history),
            Snackbar.LENGTH_LONG
        ).apply {
            setAction(getString(R.string.undo)) {
                // Restore the wallpaper
                deletedWallpaper?.let { item ->
                    viewModel.restoreToHistory(item)
                    // If we have the position, restore it in the adapter too
                    if (position >= 0) {
                        viewModel.wallpaperAdapter.restoreItem(item, position)
                    }
                    deletedWallpaper = null
                }
                undoJob?.cancel()
            }
            setActionTextColor(resources.getColor(R.color.color_theme_blue, null))
            view.setBackgroundResource(R.drawable.bg_round_corner_30)
            view.backgroundTintList = android.content.res.ColorStateList.valueOf(
                resources.getColor(R.color.color_text_primary, null)
            )
            show()
        }

        // Start countdown to clear the deleted wallpaper reference
        undoJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(4000) // Snackbar duration + buffer
            deletedWallpaper = null
        }
    }


    private fun showShimmerLoading() {
        dataBinding.shimmerLayout.visibility = View.VISIBLE
        dataBinding.shimmerLayout.startShimmer()
        dataBinding.swipeRefresh.visibility = View.GONE
        dataBinding.loutNoHistory.visibility = View.GONE
    }

    private fun hideShimmerLoading() {
        dataBinding.shimmerLayout.stopShimmer()
        dataBinding.shimmerLayout.visibility = View.GONE
        dataBinding.swipeRefresh.visibility = View.VISIBLE
    }

    // Removed batch delete functions - no selection mode

    override fun onDestroy() {
        super.onDestroy()
        undoJob?.cancel()
        deleteSnackbar?.dismiss()
    }

    private fun showEmptyState() {
        println("HistoryFragment: showEmptyState() called")

        // Immediately show empty state without animation for debugging
        dataBinding.rvHistory.visibility = View.GONE
        dataBinding.loutNoHistory.visibility = View.VISIBLE
        dataBinding.loutNoHistory.alpha = 1f
        dataBinding.loutNoHistory.scaleX = 1f
        dataBinding.loutNoHistory.scaleY = 1f

        println("HistoryFragment: Empty state visibility = ${dataBinding.loutNoHistory.visibility}")
    }

    // Temporarily disabled - menu button is commented out in layout
    /*
    private fun showHistoryMenu(anchor: View) {
        // Create custom dropdown menu
        val inflater = LayoutInflater.from(requireContext())
        val menuView = inflater.inflate(R.layout.layout_history_menu, null)

        val popupWindow = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background and animation
        popupWindow.setBackgroundDrawable(
            ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_menu)
        )
        popupWindow.elevation = 16f
        popupWindow.animationStyle = R.style.PopupMenuAnimation

        // Handle menu item clicks
        menuView.findViewById<View>(R.id.menuSelectItems).setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            viewModel.enterSelectionMode()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menuDeleteAll).setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            showClearAllDialog()
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menuSortDate).setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            // TODO: Implement sort by date
            showSnackbar("Sort by date")
            popupWindow.dismiss()
        }

        menuView.findViewById<View>(R.id.menuSortType).setOnClickListener {
            it.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            // TODO: Implement sort by type
            showSnackbar("Sort by type")
            popupWindow.dismiss()
        }

        // Show the popup menu
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        popupWindow.showAtLocation(
            anchor,
            Gravity.NO_GRAVITY,
            location[0] - 170, // Adjust X position to align right
            location[1] + anchor.height
        )
    }
    */

    // Temporarily disabled - related to menu functionality
    /*
    private fun showClearAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.clear_history))
            .setMessage(getString(R.string.remove_all_wallpapers_message))
            .setPositiveButton(getString(R.string.clear_all)) { _, _ ->
                viewModel.clearAllHistory()
                Snackbar.make(
                    dataBinding.root,
                    getString(R.string.history_cleared),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton(getString(R.string.mgs_cancel), null)
            .show()
    }
    */

    // Removed long press hint functions - no selection mode

    private fun updateEmptyState(emptyStateType: EmptyStateMessage) {
        val config = when (emptyStateType) {
            is EmptyStateMessage.NeverSetWallpaper -> EmptyStateConfig(
                icon = R.drawable.ic_image,
                title = getString(R.string.no_wallpaper_history),
                subtitle = getString(R.string.start_personalizing_device),
                actionText = getString(R.string.browse_wallpapers),
                actionType = EmptyActionType.BROWSE
            )

            is EmptyStateMessage.FilteredEmpty -> EmptyStateConfig(
                icon = R.drawable.ic_filter,
                title = getString(R.string.no_wallpapers_filter),
                subtitle = getString(R.string.try_different_filter),
                actionText = getString(R.string.clear_filters),
                actionType = EmptyActionType.CLEAR_FILTER
            )

            is EmptyStateMessage.SearchEmpty -> EmptyStateConfig(
                icon = R.drawable.ic_search,
                title = getString(R.string.no_search_results),
                subtitle = getString(R.string.try_different_keywords),
                actionText = getString(R.string.clear_search),
                actionType = EmptyActionType.CLEAR_SEARCH
            )
        }

        // Update UI
        dataBinding.emptyIcon.setImageResource(config.icon)
        dataBinding.emptyTitle.text = config.title
        dataBinding.emptySubtitle.text = config.subtitle

        if (config.actionText != null) {
            dataBinding.btnEmptyAction.visibility = View.VISIBLE
            dataBinding.btnEmptyAction.text = config.actionText
            dataBinding.btnEmptyAction.tag = config.actionType
        } else {
            dataBinding.btnEmptyAction.visibility = View.GONE
        }

        // Animate icon based on type
        when (emptyStateType) {
            is EmptyStateMessage.SearchEmpty -> {
                dataBinding.emptyIcon.animate()
                    .rotation(360f)
                    .setDuration(600)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }

            is EmptyStateMessage.FilteredEmpty -> {
                dataBinding.emptyIcon.animate()
                    .scaleX(0.8f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .withEndAction {
                        dataBinding.emptyIcon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300)
                            .start()
                    }
                    .start()
            }

            else -> {
                // Default animation for NeverSetWallpaper
                dataBinding.emptyIcon.animate()
                    .translationY(-20f)
                    .setDuration(400)
                    .withEndAction {
                        dataBinding.emptyIcon.animate()
                            .translationY(0f)
                            .setDuration(400)
                            .start()
                    }
                    .start()
            }
        }
    }

    private fun handleEmptyAction() {
        val actionType = dataBinding.btnEmptyAction.tag as? EmptyActionType ?: return

        when (actionType) {
            EmptyActionType.BROWSE -> {
                try {
                    val isFirstTimeClickBrowseHistory = localStorage.isFirstClickBrowseHistory
                    if (isFirstTimeClickBrowseHistory) {
                        AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_BROWSE_1ST)
                        localStorage.isFirstClickBrowseHistory = false
                    } else {
                        AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_BROWSE_2ND)
                    }
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                // Navigate back to browse wallpapers
                activity?.let {
                    (requireActivity() as MainActivity).updateSelectedTab(0)
                }
//                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            EmptyActionType.CLEAR_FILTER -> {
                try {
                    val isFirstTimeClickClearFilterHistory =
                        localStorage.isFirstClickClearFilterHistory
                    if (isFirstTimeClickClearFilterHistory) {
                        AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_CLEAR_FILTER_1ST)
                        localStorage.isFirstClickClearFilterHistory = false
                    } else {
                        AppConfig.logEventTracking(Constants.EventKey.HISTORY_SL_CLEAR_FILTER_2ND)
                    }
                } catch (e: Exception) {
                    Logger.e("LogEventTracking error: ${e.message}")
                }
                // Reset filter to ALL
                viewModel.setFilterType(HistoryViewModel.FilterType.ALL)
            }

            EmptyActionType.CLEAR_SEARCH -> {
                // Clear search query
                dataBinding.searchEditText.setText("")
                viewModel.setSearchQuery("")

                // Optionally hide search view
                if (dataBinding.searchEditText.isVisible) {
                    toggleSearchView()
                }
            }
        }
    }

    private fun logTracking() {
        if (localStorage.isFirstOpenHistoryScreen) {
            localStorage.isFirstOpenHistoryScreen = false
            AppConfig.logEventTracking(Constants.EventKey.HISTORY_OPEN_1ST)
        } else {
            AppConfig.logEventTracking(Constants.EventKey.HISTORY_OPEN_2ND)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deleteSnackbar?.dismiss()
        undoJob?.cancel()
    }
}