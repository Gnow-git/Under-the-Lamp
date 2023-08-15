package com.example.underthelamp.model

/**
 * 유저에 직업에 대한 정보가 저장될 data class
 */
data class UserJobDTO(
    var job : String
    ){
    constructor(): this("")
}
