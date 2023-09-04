package com.example.underthelamp.model
import com.google.firebase.Timestamp

/** 메시지 기능 DTO */
data class MessageDTO (
    var messageUser : List<String>? = null  // 대화를 같이 하는 사용자에 대한 List
    ){
    data class Chat (
        var uid : String? = null,    // 메시지를 보낸 사용자의 id 정보
        var text : String? = null,   // 메시지 내용
        var timestamp: Timestamp? = null // 메시지를 보낸 시간
    )
}
