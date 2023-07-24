package com.example.underthelamp.navigation.model

/**
 * UserCategoryInfo 정보가 저장될 data class
 */
data class UserCategoryDTO(
    var user_category : String,
    var user_goal : String,
    var user_experience : String
) {
    constructor() : this("", "", "")
}