package com.example.underthelamp.model
import com.google.firebase.Timestamp

/** 메시지 기능 DTO */
data class MessageDTO (
    var userId : String? = null, // 로그인 한 사용자
    var otherUserId : String? = null,    // 다른 사용자
    var UserMessage : String? = null,    // 로그인 한 사용자 메시지
    var otherUserMessage : String? = null,  // 다른 사용자 메시지
    var timestamp : Timestamp? = null,  // 메시지를 보낸 시간
    ){
    data class ChatRoomDTO (
        var userId : String? = null,    // 로그인 한 사용자
        var otherUserId : String? = null,   // 상대방
        var chatRoomId: String? = null  // 채팅방 Id
    )
}
