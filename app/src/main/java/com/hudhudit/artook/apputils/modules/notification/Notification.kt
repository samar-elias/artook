package com.hudhudit.artook.apputils.modules.notification

data class Notification(val id: String,
                        val client_id: String,
                        val title: String,
                        val name: String,
                        val is_seen: String,
                        val image_client: String,
                        val time: String,
                        var convertedTime: String,
                        val data_id: String,
                        val page_id: String,
                        val status: String)
