package com.abnamro.push.server.notifier


import com.abnamro.push.common.ServerApi
import com.abnamro.push.common.Token
import com.abnamro.push.common.dto.Data
import com.abnamro.push.common.dto.Notification
import com.abnamro.push.common.dto.PostRequestData
import com.squareup.okhttp.*

import java.io.IOException

interface PushSender {
    fun send(token: Token, notification: Notification?, data: Data?)
    class FcmSender(private val serverKey: ServerApi): PushSender {

        override fun send(token: Token, notification: Notification?, data: Data?) {
            val postRequestData = PostRequestData(notification, data, token.value)
            val json = postRequestData.toJsonString()
            val url = "https://fcm.googleapis.com/fcm/send"
            com.abnamro.push.server.print("Sending message $json")
            val mediaType = MediaType.parse("application/json; charset=utf-8")

            val client = OkHttpClient()

            val body = RequestBody.create(mediaType, json)
            val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "key=${serverKey.value}")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()


            val responseCallBack = object : Callback {
                override fun onFailure(request: Request, e: IOException) {
                    e.printStackTrace()
                    com.abnamro.push.server.print("Fail Message")
                }

                @Throws(IOException::class)
                override fun onResponse(response: Response) {
                    com.abnamro.push.server.print("Message sent " + response.message())
                }
            }
            val call = client.newCall(request)
            call.enqueue(responseCallBack)
        }
    }


}
