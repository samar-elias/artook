package com.hudhudit.artook.apputils.modules.post

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PostMedia(val id: String,
                     val posts_id: String,
                     val media: String,
                     val status: String): Parcelable
