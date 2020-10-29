package com.abnamro.push.server
import com.abnamro.push.common.*
import com.abnamro.push.server.notifier.PushNotifier
import com.abnamro.push.server.notifier.PushSender
import io.ktor.application.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.security.PrivateKey
import java.security.SecureRandom


val logger: Logger = LoggerFactory.getLogger("push")
fun main() {
    embeddedServer(Netty, port = 8222) {
        routing {
            static("") {
                print("Got request")
                files("src/main/static/html")
                default("src/main/static/html/index.html")
            }
            get("/start") {
                print("Got request")
                call.respondFile(File("src/main/static/html", "index.html"))

                //default("src/main/static/html/index.html")
            }
            post("/v1/send") {
                print("Got post")
                val parameters = call.receiveParameters()
                val pushServerApiKey = parameters["serveAPI"]?:""
                val pushToken = parameters["pushToken"]?:""
                val pushPublicKey = parameters["publicKey"]?:""
                val title = parameters["title"]?:""
                val message = parameters["message"]?:""
                val fallbackTitle = parameters["fallbackTitle"]?:""
                val fallbackMessage = parameters["fallbackMessage"]?:""
                val deeplink = parameters["deeplink"]?:""
                val type = parameters["type"]?:""
                val isIos = (parameters["isIos"]?:"") == "checked"
                send(
                        ServerApi(pushServerApiKey),
                        Token(pushToken),
                        PublicKey(pushPublicKey),
                        PushNotifier.MessageInput(
                                title = title,
                                message = message,
                                deeplink = deeplink,
                                type = type,
                                fallbackTitle = fallbackTitle,
                                fallbackMessage = fallbackMessage
                        ), isIos
                )
                call.respondText("It is sent 🤷. Check logs")
            }
            post("/asymmetric/encrypt") {
                print("Got post asymmetric")
                val parameters = call.receiveParameters()
                val input = parameters["encInput"]?:""
                val publicKey = parameters["encPublicKey"]?:""
                val cipherText = encryptAsymmetric(PublicKey(publicKey), input)
                call.respondText("cipher: $cipherText")
            }

            post("/asymmetric/decrypt") {
                print("Got post asymmetric")
                val parameters = call.receiveParameters()
                val input = parameters["deInput"]?:""
                val privateKey = parameters["dePrivateKey"]?:""
                val cipherText = decryptAsymmetric(privateKey, input)
                call.respondText("text: $cipherText")
            }

            post("/symmetric/decrypt") {
                print("Got post decrypt symmetric")
                val parameters = call.receiveParameters()
                val input = parameters["symDecInput"]?:""
                val secretKey = parameters["symDecSecretKey"]?:""
                val cipherText = decryptSymmetric(secretKey, input)
                call.respondText("text: $cipherText")
            }

            post("/symmetric/encrypt") {
                print("Got post to encrypt symmetric")
                val parameters = call.receiveParameters()
                val input = parameters["symEnInput"]?:""
                val (cipherText, key) = encryptSymmetric(input)
                call.respondText("key: $key cipherText: $cipherText")
            }

        }
    }.start(wait = true)
}

fun decryptSymmetric(secretKey: String, input: String): String {
    val result = cryptoManager.decryptSymmetric(input, secretKey)
    return when(result){
        is CryptoManager.CryptoResult.Error -> "Couldn't decrypt"
        is CryptoManager.CryptoResult.Data -> result.data
    }
}

fun encryptSymmetric(input: String): Pair<String, String> {
    val secretKey = cryptoManager.generateAESkey()
    val encodeKey = secretKey?.encoded?.encodeToBase64()?.let {
        String(it, Charsets.UTF_8)
    } ?: return Pair("Couldn't encrypt", "key is null")
    val result = cryptoManager.encryptSymmetric(input, secretKey)
    return when(result){
        is CryptoManager.CryptoResult.Error -> Pair("Couldn't encrypt", encodeKey)
        is CryptoManager.CryptoResult.Data -> Pair(result.data, encodeKey)
    }
}

private fun send(pushServerApiKey: ServerApi, pushToken: Token, pushPublicKey: PublicKey, input: PushNotifier.MessageInput, isIos: Boolean){
    PushNotifier.Impl(PushSender.FcmSender(pushServerApiKey)).sendFcm(pushToken, pushPublicKey, input, isIos)
}

private fun decryptAsymmetric(privateKey: String, input: String): String?{
    val result = cryptoManager.decryptAsymmetric(input, privateKey)
    return when(result){
        is CryptoManager.CryptoResult.Error -> "Couldn't decrypt"
        is CryptoManager.CryptoResult.Data -> result.data
    }
}

private fun encryptAsymmetric(publicKey: PublicKey, input: String): String?{
    val result = cryptoManager.encryptAsymmetric(input, publicKey.value)
    return when(result){
        is CryptoManager.CryptoResult.Error -> "Couldn't encrypt"
        is CryptoManager.CryptoResult.Data -> result.data
    }
}

fun print(o: Any) {
    println("$o")
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