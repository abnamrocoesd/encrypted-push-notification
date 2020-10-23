package com.abnamro.push.server


import com.abnamro.push.common.ext.toText
import com.abnamro.push.server.notifier.PushNotifier
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO


var pushServerApiKey = "AAAA5UJS9_U:APA91bEOMjC_KJbH3qrN0Zouowyd2g7KMKBiAUVvrOapwPV7ojFEHADIrH9btutFlh0zTIeKpd-w0PbfSeKeJhoGAhO_aBMuRarEt6tkHkouTjTgY7UDs2WfINW-8zA0u6E7T-XzGS_u4b8bwCi7-HzFrF1XotBstA"//From your firebase project
var pushToken = "cNdooQCeSJmFYTsUyx8xRS:APA91bFoLn3Ni0fkBe_F_-5g8vBm3jDjYH5iUa-ZpoJrQvTsSpAJcd_w5h69TxLyqYqSjT273tu1eNsFf7vL3GmzxXIosFMyqWyRda--5G_3qQR0KLYcG87E-Nsk_mNmkq52bxMoG3dH" //device push notification token
var pushPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAohGUVZSh1QHrthlszSlkq4R376te8M70\n" +
        "    p1bsiAqhvYHAHM6xrMnR1eDbU9Vsb56/BVCjw1wgjGuHYbcCxvKtIYcn+KZ/UZnLsniQBwAui5Nw\n" +
        "    nHao6jZmORhIHEAY6MO1KBNFhGcN+JY+KGdJN6b7QEKST8m+u2swRWK5D5RBp/K6/2KVPgIBZ2bZ\n" +
        "    WwYL9g+lcqNDDTe4zr6W/ayeWCrvrk/4AVFZIdxJ2jF4jWxy6uUlxJW3rHSTcwAAePxHtTNVJp3n\n" +
        "    U5CYpa3qBpdkRP9gxpJ+o9uwB4wz5Q6vsRUWxuFkMi4Hp5Ylikl8ducZ4TseyZyU30hnGZSxY2tD\n" +
        "    T2aX+wIDAQAB"//device public key

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
