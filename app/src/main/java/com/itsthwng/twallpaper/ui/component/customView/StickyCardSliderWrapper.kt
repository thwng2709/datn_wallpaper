package com.itsthwng.twallpaper.ui.component.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.github.islamkhsh.CardSliderViewPager
import com.github.islamkhsh.viewpager2.ViewPager2
import com.itsthwng.twallpaper.R

class StickyCardSliderWrapper @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val internalViewPager: CardSliderViewPager =
        LayoutInflater.from(context).inflate(R.layout.layout_inner_card_slider, this, false)
            .findViewById(R.id.innerCardSlider)

    /** Danh sách vị trí “sticky” thủ công (nếu bạn muốn cố định vài item cụ thể) */
    private val stickyPositions = mutableSetOf<Int>()

    /** Predicate để quyết định có được auto-slide tại vị trí hiện tại không (ví dụ: isAd) */
    private var shouldAutoSlideAt: ((position: Int) -> Boolean)? = null

    private var originalAutoSlideTime: Int? = null
    private var userPaused = false

    init {
        addView(internalViewPager)
        internalViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                reevaluateAutoSlide(position)
            }
        })
    }

    fun getViewPager(): CardSliderViewPager = internalViewPager

    /** Dùng khi muốn “sticky” vài item cố định (không liên quan ads) */
    fun setStickyPositions(positions: Collection<Int>) {
        stickyPositions.clear()
        stickyPositions.addAll(positions)
        reevaluateAutoSlide(internalViewPager.currentItem)
    }

    /** Chỉ định logic cho phép auto-slide tại 1 vị trí. Ví dụ: { pos -> !items[pos].isAd } */
    fun setShouldAutoSlidePredicate(predicate: (position: Int) -> Boolean) {
        this.shouldAutoSlideAt = predicate
        // đánh giá lại ngay vị trí hiện tại
        reevaluateAutoSlide(internalViewPager.currentItem)
    }

    /** Dừng ngay (giữ nguyên item hiện tại) — ưu tiên cao nhất */
    fun pauseAutoSlide() {
        if (!userPaused) {
            userPaused = true
            if (originalAutoSlideTime == null)
                originalAutoSlideTime = internalViewPager.autoSlideTime
            internalViewPager.autoSlideTime = CardSliderViewPager.STOP_AUTO_SLIDING
        }
    }

    /** Tiếp tục auto-slide (nếu không đứng trên item bị chặn) */
    fun resumeAutoSlide() {
        if (userPaused) {
            userPaused = false
            if (!isBlocked(internalViewPager.currentItem)) {
                restoreAutoSlide()
            } // còn đang đứng trên item chặn (vd: ads) thì giữ nguyên dừng
        }
    }

    /** Gọi khi dữ liệu thay đổi/chèn ads để đánh giá lại (optional nhưng nên có) */
    fun recheckNow() {
        reevaluateAutoSlide(internalViewPager.currentItem)
    }

    private fun reevaluateAutoSlide(position: Int) {
        if (userPaused) {
            if (originalAutoSlideTime == null) originalAutoSlideTime = internalViewPager.autoSlideTime
            internalViewPager.autoSlideTime = CardSliderViewPager.STOP_AUTO_SLIDING
            return
        }

        if (isBlocked(position)) {
            if (originalAutoSlideTime == null) originalAutoSlideTime = internalViewPager.autoSlideTime
            internalViewPager.autoSlideTime = CardSliderViewPager.STOP_AUTO_SLIDING
        } else {
            restoreAutoSlide()
        }
    }

    private fun restoreAutoSlide() {
        val restore = originalAutoSlideTime ?: internalViewPager.autoSlideTime
        internalViewPager.autoSlideTime = restore
        originalAutoSlideTime = null
    }

    /** Bị chặn auto-slide khi: thuộc stickyPositions HOẶC predicate trả false */
    private fun isBlocked(position: Int): Boolean {
        if (position in stickyPositions) return true
        val allow = shouldAutoSlideAt?.invoke(position) ?: true
        return !allow
    }
}
