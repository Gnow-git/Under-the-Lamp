package com.example.underthelamp.model
import com.google.firebase.Timestamp

// 커뮤니티의 게시글에 대한 DTO
data class ContestDTO(
    var contestTitle: String? = null,  //제목
    var imageUrl: String? = null,   // 이미지
    var contestContent: String? = null,    // 내용
    var hashTag: List<String>? = null,  // 해시태그
    var uid: String? = null,    // 게시글 ID
    var userId: String? = null, // 작성자 ID
    var timestamp : Timestamp? = null,
    var likeCount: Int = 0, // 좋아요 개수
    var likes: MutableMap<String, Boolean> = HashMap()) {
        data class ContestComment(      // 커뮤니티 댓글에 대한 DTO
            var uid: String? = null,    // 댓글 ID
            var userId: String? = null, // 댓글 작성자 ID
            var comment: String? = null,    // 댓글 내용
            var timestamp : Timestamp? = null
        )
}