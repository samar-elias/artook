package com.hudhudit.artook.apputils.modules.competition

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Contest(val id: String,
                   val title: String,
                   val description: String,
                   val image: String,
                   val date: String,
                   val first_prize: String,
                   val secand_prize: String,
                   val third_prize: String,
                   val date_time: String,
                   val flag: String,
                   val status: String): Parcelable

data class ContestResult(val status: Status, val results: Contest)