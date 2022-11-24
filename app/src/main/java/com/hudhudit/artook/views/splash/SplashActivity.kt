package com.hudhudit.artook.views.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.hudhudit.artook.apputils.appdefs.AppDefs
import com.hudhudit.artook.apputils.helpers.LocaleHelper
import com.hudhudit.artook.apputils.modules.user.UserData
import com.hudhudit.artook.R
import com.hudhudit.artook.views.main.MainActivity
import com.hudhudit.artook.views.registration.RegistrationActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setSplash()
    }

    private fun setSplash() {
        Handler(Looper.myLooper()!!).postDelayed(
            {
                getUserFromSharedPreferences()
            }, 2000)
    }

    private fun getUserFromSharedPreferences() {
        val sharedPreferences = getSharedPreferences(AppDefs.SHARED_PREF_KEY, MODE_PRIVATE)
        val id = sharedPreferences.getString(AppDefs.ID_KEY, null)
        val gson = Gson()
        val user = sharedPreferences.getString(AppDefs.USER_KEY, null)
        when {
            id != null && user != null-> {
                AppDefs.user = gson.fromJson(user, UserData::class.java)
                AppDefs.lang = sharedPreferences.getString(AppDefs.Language_KEY, null)
                LocaleHelper.setAppLocale(AppDefs.lang, this)
                val mainIntent = Intent(this, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            else -> {
                val registrationIntent = Intent(this, RegistrationActivity::class.java)
                startActivity(registrationIntent)
                finish()
            }
        }
    }
}