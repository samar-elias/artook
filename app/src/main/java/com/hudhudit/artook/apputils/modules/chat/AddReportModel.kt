package com.hudhudit.artook.apputils.modules.chat

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddReportModel(

    var typeReport: String = "",
    var companyName: String = "",
    var companyReportName: String = "",
    var companyId: String = "",
    var companyReportId: String = "",
    var companyLogo: String = "",
    var chatId: String = "",
    var reportId: String = "",


    ) : Parcelable