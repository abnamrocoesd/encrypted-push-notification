package com.abnamro.push.server


import com.abnamro.push.common.dto.Data
import com.abnamro.push.common.dto.Notification
import com.abnamro.push.common.dto.PostRequestData
import com.abnamro.push.server.notifier.toJsonString
import com.google.gson.Gson
import com.squareup.okhttp.*

import java.io.IOException

interface PushSender {
    fun send(token: String, notification: Notification?, data: Data?)
    class FcmSender(private val serverKey: String): PushSender {

        override fun send(token: String, notification: Notification?, data: Data?) {
            val postRequestData = PostRequestData(notification, data, token)
            val json = postRequestData.toJsonString()
            val url = "https://fcm.googleapis.com/fcm/send"
            print("Sending message $json")
            val mediaType = MediaType.parse("application/json; charset=utf-8")

            val client = OkHttpClient()

            val body = RequestBody.create(mediaType, json)
            val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "key=$serverKey")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()


            val responseCallBack = object : Callback {
                override fun onFailure(request: Request, e: IOException) {
                    e.printStackTrace()
                    print("Fail Message")
                }

                @Throws(IOException::class)
                override fun onResponse(response: Response) {
                    print("Message sent " + response.message())
                }
            }
            val call = client.newCall(request)
            call.enqueue(responseCallBack)
        }
    }


}
