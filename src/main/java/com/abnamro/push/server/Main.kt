package com.abnamro.push.server


import com.abnamro.push.server.notifier.PushNotifier
import java.io.File
import java.util.*

var pushServerApiKey = ""//From your firebase project
var pushToken = ""
var pushPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqNe8d4WEYt3Xkt25oTUJluptFTCyQSw1\n" +
        "    wqPtaGgH3i2DI5fPRprpP9s8dW8W6WBzHVyzoiSxfU24CiBly2kZF+XLAFT20Lo+XIDrDA+3YJ+H\n" +
        "    quLAP8AmHCol8jK8gp7wAERnyYgmMEtQ+V6c0PvbWWQpRnNdXsqg55oaURspZVQHEyxW1Nl3pjPo\n" +
        "    NM3rsWWYB6C/88F5mtCFsUpicFD6DSpshEu/hb0Q8CULWQwzSIATTD7QOLiUB5FRwQiS34I7bKPo\n" +
        "    TRiA7j7lNUsHsULuj+CZIgHBkkufPI0M3CL+b4eQyvgbCKqByE3SJTrhDKUVJcFmUknVCFSgcg0r\n" +
        "    Iv3QJQIDAQAB"

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
    PushNotifier.Impl(PushSender.FcmSender(pushServerApiKey)).sendFcm(pushToken, pushPublicKey, input)
    return
}

fun readArgs(args: Array<out String>) {
    if(args.size == 3){

        pushServerApiKey = args[0].fromFile()
        pushToken = args[1].fromFile()
        pushPublicKey = args[2].fromFile()
    }
}

fun String.fromFile() = File(this).readText(Charsets.UTF_8)


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
