package com.itsthwng.twallpaper.ui.component.history.model

import androidx.annotation.DrawableRes

sealed class EmptyStateMessage {
    object NeverSetWallpaper : EmptyStateMessage()
    object FilteredEmpty : EmptyStateMessage()
    object SearchEmpty : EmptyStateMessage()
}

data class EmptyStateConfig(
    @DrawableRes val icon: Int,
    val title: String,
    val subtitle: String,
    val actionText: String? = null,
    val actionType: EmptyActionType = EmptyActionType.BROWSE
)

enum class EmptyActionType {
    BROWSE,
    CLEAR_FILTER,
    CLEAR_SEARCH
}