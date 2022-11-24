package com.hudhudit.artook.apputils.modules.competition

import com.hudhudit.artook.apputils.modules.status.Status

data class Participant(val id: String,
                       val contests_id: String,
                       val description: String,
                       val name: String,
                       val imageClient: String,
                       val image: String,
                       var count_vote: String,
                       var is_vote: String,
                       val status: String)

data class Participants(val count: String,
                        val data: ArrayList<Participant>)

data class ParticipantsResult(val status: Status,
                              val results: Participants)
