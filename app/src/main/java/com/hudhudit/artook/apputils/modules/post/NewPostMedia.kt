package com.hudhudit.artook.apputils.modules.post

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NewPostMedia(var mediaUri: Uri, var mediaType: String, var index: Int): Parcelable
