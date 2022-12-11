package com.hudhudit.artook.apputils.appdefs

import android.content.SharedPreferences
import androidx.core.content.edit
import com.hudhudit.artook.apputils.appdefs.AppConstants.Companion.DEVICE_TOKEN

import java.util.*
import javax.inject.Inject

class Prefs @Inject constructor(
    private val sharedPrefs: SharedPreferences,
) {


    fun saveDeviceToken(token: String){
        val editor =sharedPrefs.edit()
        editor.putString(DEVICE_TOKEN, token)
        editor.apply()
    }
    fun featchDeviceToken():String ?{
        return sharedPrefs.getString(DEVICE_TOKEN, null)
    }

}