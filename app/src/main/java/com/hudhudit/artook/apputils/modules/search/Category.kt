package com.hudhudit.artook.apputils.modules.search

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(val id: String,
                    val title: String,
                    val status: String): Parcelable

data class Categories(val status: Status, val results: ArrayList<Category>)
