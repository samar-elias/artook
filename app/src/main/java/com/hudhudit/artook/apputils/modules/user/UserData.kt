package com.hudhudit.artook.apputils.modules.user

import com.hudhudit.artook.apputils.modules.status.Status

data class UserData(val status:Status,
                    val token: String?,
                    val results: UserResult?)
