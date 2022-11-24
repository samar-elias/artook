package com.hudhudit.artook.apputils.modules.competition

import com.hudhudit.artook.apputils.modules.status.Status

data class Vote(val count_vote: String)

data class VoteResult(val status: Status,
                      val results: Vote)
