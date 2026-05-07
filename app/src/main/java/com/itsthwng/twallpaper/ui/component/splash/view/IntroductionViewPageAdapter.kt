package com.itsthwng.twallpaper.ui.component.splash.view

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class IntroductionViewPageAdapter(
    fragment: Fragment,
    private val introductionItems: List<IntroductionItem>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return introductionItems.size
    }

    override fun createFragment(position: Int): Fragment {
        return IntroductionItemFragment.newInstance(introductionItems[position])
    }
}
