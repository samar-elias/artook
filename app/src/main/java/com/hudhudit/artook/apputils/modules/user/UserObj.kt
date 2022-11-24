package com.hudhudit.artook.apputils.modules.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserObj(val name: String,
                   val user_name: String,
                   val email: String,
                   val phone: String,
                   val password: String ): Parcelable
