package com.abnamro.push.server.notifier

import com.abnamro.push.common.dto.Content
import com.abnamro.push.common.dto.Data
import com.abnamro.push.common.dto.EncryptedData
import com.abnamro.push.common.CryptoManager
import com.abnamro.push.common.LogBridge
import com.abnamro.push.common.SecureRandomBridge
import com.abnamro.push.common.encodeToBase64
import com.abnamro.push.server.PushSender
import com.abnamro.push.server.print
import com.google.gson.Gson
import java.lang.IllegalArgumentException
import java.security.SecureRandom
import javax.crypto.SecretKey

interface PushNotifier {
    data class MessageInput(val title: String, val message: String, val fallbackTitle: String, val fallbackMessage: String, val deeplink: String, val type: String)

    fun sendFcm(token: String, publicKey: String, input: MessageInput)
    class Impl(private val pushSender: PushSender): PushNotifier {
        data class Message(val title: String, val message: String, val deeplink: String, val type: String)

        fun encryptMessage(aesKey: SecretKey?, data: Message): CryptoManager.CryptoResult {
            if(aesKey == null) {
                return CryptoManager.CryptoResult.Error(NullPointerException())
            }
            val data = EncryptedData(
                    title =data.title,
                    message = data.message,
                    type = data.type,
                    url = data.deeplink
            )
            val payloadStr = data.toJsonString()
            print("encrypting $payloadStr")

            return encryptMessage(aesKey, payloadStr)

        }

        private val cryptoManager = CryptoManager.Impl(
                object : LogBridge {
                    override fun debug(message: String?, e: Throwable?) {
                        println("$message")
                        e?.printStackTrace()
                    }

                    override fun error(message: String?, e: Throwable?) {
                        debug(message, e)
                    }

                },
                object : SecureRandomBridge {
                    private val random = SecureRandom.getInstanceStrong()
                    override fun nextBytes(b: ByteArray) {
                        random.nextBytes(b)
                    }

                    override fun getImpl() = random

                }
        )

        private fun encryptMessage(aesKey: SecretKey, payloadStr: String): CryptoManager.CryptoResult {
            val payloadEncrypted = cryptoManager.encryptSymmetric(payloadStr, aesKey)
            print("encrypted $payloadEncrypted")
            return payloadEncrypted
        }


        override fun sendFcm(token: String, publicKey: String, input: MessageInput) {

            print("Token: $token")
            print("publicKey: $publicKey")


            val aesKey = cryptoManager.generateAESkey()
            val  payload = encryptedMessage(input, aesKey)

            val encryptedEncodedAesKey = encryptAesKey(aesKey, publicKey)

            val content = when(encryptedEncodedAesKey){
                is CryptoManager.CryptoResult.Error -> Content(
                        version = "v2",
                        title = input.fallbackTitle,
                        message = input.fallbackMessage,
                        type = input.type,
                        key = "",
                        payload = payload
                )
                is CryptoManager.CryptoResult.Data -> Content(
                        version = "v2",
                        title = input.fallbackTitle,
                        message = input.fallbackMessage,
                        type = input.type,
                        key = encryptedEncodedAesKey.data,
                        payload = payload
                )
            }

            val data = Data(content)

            pushSender.send(token, data)
        }

        private fun encryptAesKey(aesKey: SecretKey?, publicKey: String): CryptoManager.CryptoResult {
            if(aesKey == null){
                return CryptoManager.CryptoResult.Error(IllegalArgumentException())
            }

            val encode = aesKey.encoded.encodeToBase64()
            print("AES key bytes: ${encode.toHexArray()}")
            val encodedAesKeyStr =String(encode, Charsets.UTF_8)
            print("AES key string: $encodedAesKeyStr")

            return cryptoManager.encryptAsymmetric(encodedAesKeyStr, publicKey)
        }

        private fun encryptedMessage(input: MessageInput, aesKey: SecretKey?): String {
            val title = input.title
            val message = input.message
            val url = input.deeplink

            val payloadResult = encryptMessage(
                    aesKey = aesKey,
                    data = Message(
                            title = title,
                            message = message,
                            deeplink = url,
                            type = input.type
                    )
            )
            val payload = when (payloadResult) {
                is CryptoManager.CryptoResult.Error -> ""
                is CryptoManager.CryptoResult.Data -> payloadResult.data
            }
            return payload
        }

    }
}

fun ByteArray.toHexArray(): List<String>{
    return this.map { b-> String.format("%02X", b) }
}
val gson = Gson()
fun Any.toJsonString() = gson.toJson(this)



