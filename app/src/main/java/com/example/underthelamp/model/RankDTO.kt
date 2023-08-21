package com.example.underthelamp.model

/** informationRank 에 대한 DTO*/
data class RankDTO(
    var rankTitle: String? = null,  // 랭킹 제목
    var imageUrl: String? = null,   // 배너에 나오게 될 이미지
    var uid: String? = null,    // rank 에 등록된 게시글 ID
    var timestamp : Long? = null,   // 특정 주소에 rank 가 등록된 시간
    var rankLikeCount: Int = 0, // 랭크 주제에 해당 하는 공모전 의 좋아요 개수 합
)