package com.abnamro.push.server
import com.abnamro.push.common.PublicKey
import com.abnamro.push.common.ServerApi
import com.abnamro.push.common.Token
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
        }
    }.start(wait = true)
}

private fun send(pushServerApiKey: ServerApi, pushToken: Token, pushPublicKey: PublicKey, input: PushNotifier.MessageInput, isIos: Boolean){
    PushNotifier.Impl(PushSender.FcmSender(pushServerApiKey)).sendFcm(pushToken, pushPublicKey, input, isIos)
}

fun print(o: Any) {
    println("$o")
}
