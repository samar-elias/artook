package com.hudhudit.artook.apputils.modules.notification

import com.hudhudit.artook.apputils.modules.status.Status

data class Notifications(val count: String,
                         val data: ArrayList<Notification>)

data class NotificationsResult(val status: Status,
                               val results: Notifications?)
