package com.itsthwng.twallpaper.ui.component.splash.view

import androidx.annotation.DrawableRes

data class IntroductionItem(
    var title: String,
    var description: String,
    @DrawableRes var imageResource: Int,
)
