package com.itsthwng.twallpaper.ui.component.splash.view

import android.os.Bundle
import com.itsthwng.twallpaper.R
import com.itsthwng.twallpaper.databinding.FragmentIntroItemBinding
import com.itsthwng.twallpaper.ui.base.BaseFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroductionItemFragment : BaseFragmentBinding<FragmentIntroItemBinding>() {

    private lateinit var title: String
    private lateinit var description: String
    private var imageResource = 0

    override fun getContentViewId() = R.layout.fragment_intro_item

    override fun initializeViews() {
        if (arguments != null) {
            title = requireArguments().getString(ARG_TITLE)!!
            description = requireArguments().getString(ARG_DESCRIPTION)!!
            imageResource = requireArguments().getInt(ARG_IMAGE_RESOURCE)
        }
        dataBinding.introductionItem = IntroductionItem(title, description, imageResource)
        when (imageResource) {
            1 -> dataBinding.imgIntro.setImageResource(R.drawable.image_intro_1)
            2 -> dataBinding.imgIntro.setImageResource(R.drawable.image_intro_2)
            3 -> dataBinding.imgIntro.setImageResource(R.drawable.image_intro_3)
            4 -> dataBinding.imgIntro.setImageResource(R.drawable.image_intro_4)
        }

    }

    override fun registerListeners() {

    }

    override fun initializeData() {

    }

    companion object {
        private const val ARG_TITLE = "argTitle"
        private const val ARG_DESCRIPTION = "argDescription"
        private const val ARG_IMAGE_RESOURCE = "argImageResource"
        fun newInstance(
            introductionItem: IntroductionItem
        ): IntroductionItemFragment {
            val fragment = IntroductionItemFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, introductionItem.title)
            args.putString(ARG_DESCRIPTION, introductionItem.description)
            args.putInt(ARG_IMAGE_RESOURCE, introductionItem.imageResource)
            fragment.arguments = args
            return fragment
        }
    }
}