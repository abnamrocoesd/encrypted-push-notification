package com.abnamro.push.server


import com.abnamro.push.common.ext.toText
import com.abnamro.push.server.notifier.PushNotifier
import com.abnamro.push.server.notifier.PushSender
import java.io.File
import java.util.*


var pushServerApiKey = ""//From your firebase project
var pushToken = "" //device push notification token
var pushPublicKey = ""//device public key

fun main(vararg args: String) {
    readArgs(args)
    if(pushServerApiKey.isEmpty()){
        print("Please provide a server key, see Main.kt")
        System.exit(0)
    }
    if(pushToken.isEmpty()){
        print("Please provide a token, see Main.kt")
        System.exit(0)
    }
    if(pushPublicKey.isEmpty()){
        print("Please provide a public key, see Main.kt")
        System.exit(0)
    }
    print("API key $pushServerApiKey")
    print("token $pushToken")
    print("public key $pushPublicKey")
    val input = PushNotifier.MessageInput(
            title = prompt("title"),
            message = prompt("message"),
            deeplink = prompt("Deeplink"),
            type = prompt("Type"),
            fallbackTitle = "Fallback title encryption failed",
            fallbackMessage = "Fallback message encryption failed"
    )
    val isIos = prompt("iOS? (y|n)")
    PushNotifier.Impl(PushSender.FcmSender(pushServerApiKey)).sendFcm(pushToken, pushPublicKey, input, isIos?.equals("y", true))
}

fun readArgs(args: Array<out String>) {
    if(args.size == 3) {
        pushServerApiKey = File(args[0]).toText()
        pushToken = File(args[1]).toText()
        pushPublicKey = File(args[2]).toText()
    }
}

private val scanner = Scanner(System.`in`)
fun print(o: Any) {
    println("" + o)
}

fun prompt(title: String): String {
    print("$title>")
    val t = scanner.nextLine()
    println()

    return t

}
