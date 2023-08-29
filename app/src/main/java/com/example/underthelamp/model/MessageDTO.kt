package com.example.underthelamp.model
import com.google.firebase.Timestamp

/** 메시지 기능 DTO */
data class MessageDTO (
    var receiveUserId : String? = null,
    var sendUserId : String? = null,
    var receiveUserMessage : String? = null,
    var sendUserMessage : String? = null,
    var timestamp : Timestamp? = null )