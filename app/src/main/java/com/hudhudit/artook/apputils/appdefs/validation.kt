package com.hudhudit.artook.apputils.appdefs

import android.content.Context
import android.text.format.DateFormat
import com.hudhudit.artook.R

import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody

fun getTimeAgo(time: Long,context: Context): String? {

    var time = time

    if (time < 1000000000000L) {
        time *= 1000
    }
    val now = System.currentTimeMillis()
    if (time > now || time <= 0) {
        return null
    }

    val diff = now - time

    return when {
        diff < AppConstants.MINUTE_MILLIS -> {
            context.resources.getString(R.string.just_now)

        }
        diff < 2 * AppConstants.MINUTE_MILLIS -> {

            context.resources.getString(R.string.a_minute_ago)
        }
        diff < 50 * AppConstants.MINUTE_MILLIS -> {
            (diff / AppConstants.MINUTE_MILLIS).toString() + " "+ context.resources.getString(R.string.minutes_ago)
        }
        diff < 90 * AppConstants.MINUTE_MILLIS -> {

            context.resources.getString(R.string.an_hour_ago)
        }
        diff < 24 * AppConstants.HOUR_MILLIS -> {
            (diff / AppConstants.HOUR_MILLIS).toString() + " "+  context.resources.getString(R.string.hours_ago)
        }
        diff < 48 * AppConstants.HOUR_MILLIS -> {
            context.resources.getString(R.string.yesterday)
        }
        else -> {
            (diff / AppConstants.DAY_MILLIS).toString() + " "+  context.resources.getString(R.string.days_ago)
        }
    }
}

fun getMyPrettyDate(neededTimeMilis: Long,context: Context): String? {
    val nowTime = Calendar.getInstance()
    val neededTime = Calendar.getInstance()
    neededTime.timeInMillis = neededTimeMilis
    return if (neededTime[Calendar.YEAR] == nowTime[Calendar.YEAR]) {
        if (neededTime[Calendar.MONTH] == nowTime[Calendar.MONTH]) {
            if (neededTime[Calendar.DATE] - nowTime[Calendar.DATE] == 1) {
                //here return like "Tomorrow at 12:00"
                context.resources.getString(R.string.tomorrow)
            } else if (nowTime[Calendar.DATE] == neededTime[Calendar.DATE]) {
                //here return like "Today at 12:00"
                context.resources.getString(R.string.today)
            } else if (nowTime[Calendar.DATE] - neededTime[Calendar.DATE] == 1) {
                //here return like "Yesterday at 12:00"
                context.resources.getString(R.string.yesterday)
            } else {
                //here return like "May 31, 12:00"
                DateFormat.format("MMMM d yyyy", neededTime).toString()
            }
        } else {
            //here return like "May 31, 12:00"
            DateFormat.format("MMMM d yyyy", neededTime).toString()
        }
    } else {
        //here return like "May 31 2010, 12:00" - it's a different year we need to show it
        DateFormat.format("MMMM dd yyyy", neededTime).toString()
    }


}
fun createRequestBody(parameter: String?): RequestBody {
    return RequestBody.create("multipart/form-data".toMediaTypeOrNull(), parameter!!)
}
fun createMultipartBodyPart(name: String?, imagePath2: String): MultipartBody.Part? {
    if (imagePath2 == null || !File(imagePath2).exists()) {
        return null
    }
    val imageFile = File(imagePath2)

    val requestBody = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), imageFile)

    // MultipartBody.Part is used to send also the actual file name
    return try {
        MultipartBody.Part.createFormData(name!!, URLEncoder.encode(imageFile.name, "utf-8"), requestBody)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
        null
    }
}
