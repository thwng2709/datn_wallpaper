package com.itsthwng.twallpaper.notification

object NotifyConstants {

    object TrackingTag {
        const val NOTIFY_D1 = "notify_d1"
        const val NOTIFY_D2 = "notify_d2"
        const val NOTIFY_D7 = "notify_d7"
        const val NOTIFY_SATURDAY = "notify_saturday"
        const val NOTIFY_AFTER_10M = "notify_after_10m"
        const val NOTIFY_UNKNOW_TAG = "notify_unknow_tag"
        const val NOTIFY_AFTER_4D_FAVORITE_DOC = "notify_after_4d_favorite_doc"
        const val NOTIFY_AFTER_12H_RECENT_DOC = "notify_after_1d_recent_doc"
    }

    object NotifyType {
        const val NOTIFY_TYPE_D1 = "D1"
        const val NOTIFY_TYPE_D2 = "D2"
        const val NOTIFY_TYPE_D7 = "D7"
        const val NOTIFY_TYPE_SATURDAY = "weekly"
        const val NOTIFY_TYPE_AFTER_10M = "10m"
        const val NOTIFY_TYPE_FAVORITE_DOC = "D4"
        const val NOTIFY_TYPE_RECENT_DOC = "recent"
    }

    const val NOTIFICATION_ACTION_KEY = "local_notify_action"
    const val NOTIFICATION_INTENT_KEY = "notificationIntentKey"
    const val NOTIFICATION_TRACKING_TAG = "notify_tracking_tag"
}