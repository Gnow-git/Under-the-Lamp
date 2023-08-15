package com.example.underthelamp.navigation.util

import com.example.underthelamp.BuildConfig
import com.example.underthelamp.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import io.grpc.Server
import okhttp3.*
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

class FcmPush {

    var JSON = "application/json; charset=utf-8".toMediaTypeOrNull()
    var url = "https://fcm.googleapis.com/fcm/send"
    var serverKey = "${BuildConfig.SERVER}"
    var gson: Gson? = null
    var okHttpClient: OkHttpClient? = null

    companion object {
        var instance = FcmPush()
    }

    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var token = task?.result?.get("pushToken").toString()

                    var pushDTO = PushDTO()
                    pushDTO.to = token
                    pushDTO.notification.title = title
                    pushDTO.notification.body = message

                    // okhttp를 통해서 message 전송
                    //var jsonObject = JsonObject()
                    //var body = jsonObject.toString().toRequestBody(JSON)
                    var body = gson?.toJson(pushDTO)!!.toRequestBody(JSON)

                    //var body = RequestBody.create(JSON, gson?.toJson(pushDTO).toString())
                    var request = Request.Builder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "key="+serverKey)
                        .url(url)
                        .post(body)
                        .build()

                    okHttpClient?.newCall(request)?.enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        override fun onResponse(call: Call, response: Response) {
                            println(response?.body?.string())
                        }

                    })
                }
            }
    }
}