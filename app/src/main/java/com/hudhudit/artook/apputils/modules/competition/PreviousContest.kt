package com.hudhudit.artook.apputils.modules.competition

import android.os.Parcelable
import com.hudhudit.artook.apputils.modules.status.Status
import kotlinx.parcelize.Parcelize

@Parcelize
data class PreviousContest(val id: String,
                           val title: String,
                           val description: String,
                           val image: String,
                           val date: String,
                           val date_time: String,
                           val status: String): Parcelable

data class PreviousContests(val count: String,
                            val data: ArrayList<PreviousContest>)

data class PreviousContestsResult(val status: Status,
                                  val results: PreviousContests)
