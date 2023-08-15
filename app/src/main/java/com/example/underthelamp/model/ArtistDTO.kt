package com.example.underthelamp.model

/**
 * 예술인 찾기할 때 나타낼 유저 정보
 */
data class ArtistDTO(
    var name: String? = null,
    var category: String? = null,
    var uid: String? = null
) {
    constructor() : this("", "", "")
}