package com.hudhudit.artook.apputils.modules.booleanresponse

import com.hudhudit.artook.apputils.modules.status.Status

data class BooleanResponse(val status: Status,
                           val results: Boolean?)
