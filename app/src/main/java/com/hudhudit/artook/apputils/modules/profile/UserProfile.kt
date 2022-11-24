package com.hudhudit.artook.apputils.modules.profile

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(val id: String,
                       val name: String,
                       val user_name: String,
                       val image_client: String,
                       val email: String,
                       val bio: String,
                       val followers: String,
                       val following: String,
                       var is_following: String): Parcelable

data class UserProfileData(val status: Status,
                           val results: UserProfile)
