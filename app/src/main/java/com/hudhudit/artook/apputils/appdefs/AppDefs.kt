package com.hudhudit.artook.apputils.appdefs

import com.hudhudit.artook.apputils.modules.user.UserData
import java.util.*

class AppDefs  {
    companion object{
        var SHARED_PREF_KEY = "SHARED_PREF"
        var ID_KEY = "ID"
        var USER_KEY = "USER"
        var Language_KEY = "LANG"
        var lang: String? = "en"
        const val INBOX_PATH = "chatroom"
        const val USER_PATH = "users"
        var isFeed = true

        lateinit var user: UserData

        @JvmName("getLanguage1")
        fun getLanguage(): String? {
            return if (Locale.getDefault().displayLanguage == "العربية") {
                "ar"
            } else {
                "en"
            }
        }
    }
}