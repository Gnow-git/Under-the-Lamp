package com.example.underthelamp.model

/**
 * 유저에 대한 정보가 저장될 data class
 */
data class UserDTO(
    var uId : String,
    var email : String,

){
    constructor(): this("","")
}
