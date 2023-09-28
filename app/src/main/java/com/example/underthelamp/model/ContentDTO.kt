package com.example.underthelamp.model
import com.google.firebase.Timestamp

data class ContentDTO(var explain : String? = null,
                      var imageUrl : String? = null,
                      var contentUid : String? = null,
                      var userId : String? = null,
                      var timestamp : Timestamp? = null,
                      var favoriteCount : Int = 0,
                      var favorites : MutableMap<String,Boolean> = HashMap()) {
    data class Comment(var uid : String? = null,
                       var userId : String? = null,
                       var comment : String? = null,
                       var timestamp : Timestamp? = null)
}