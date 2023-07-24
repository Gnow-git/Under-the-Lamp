package com.example.underthelamp.navigation.model
/**
 * UserInfo 정보가 저장될 data class
 */
data class UserDetailDTO(

    var user_name: String,
    var user_gender: String,
    var user_age: String,
    var user_location: String,
    var user_sub_category: String
) {
    constructor() : this("", "", "", "", "")
}

