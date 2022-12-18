package com.hudhudit.artook.apputils.appdefs

import android.net.Uri
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
        var media1Uri: Uri? = null
        var media2Uri: Uri? = null
        var media3Uri: Uri? = null
        var media4Uri: Uri? = null
        var media5Uri: Uri? = null
        var media6Uri: Uri? = null
        lateinit var user: UserData
        var homePage = 1
        var homePosition = 0

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