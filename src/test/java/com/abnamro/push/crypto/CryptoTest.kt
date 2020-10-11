package com.abnamro.push.crypto

import com.abnamro.push.server.notifier.toHexArray
import org.junit.Test
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.fail

private const val PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCo17x3hYRi3deS3bmhNQmW6m0V\n" +
        "    MLJBLDXCo+1oaAfeLYMjl89Gmuk/2zx1bxbpYHMdXLOiJLF9TbgKIGXLaRkX5csAVPbQuj5cgOsM\n" +
        "    D7dgn4eq4sA/wCYcKiXyMryCnvAARGfJiCYwS1D5XpzQ+9tZZClGc11eyqDnmhpRGyllVAcTLFbU\n" +
        "    2XemM+g0zeuxZZgHoL/zwXma0IWxSmJwUPoNKmyES7+FvRDwJQtZDDNIgBNMPtA4uJQHkVHBCJLf\n" +
        "    gjtso+hNGIDuPuU1SwexQu6P4JkiAcGSS588jQzcIv5vh5DK+BsIqoHITdIlOuEMpRUlwWZSSdUI\n" +
        "    VKByDSsi/dAlAgMBAAECggEAHt0w6nhRtvDukz5MzJcxZZD8177LFvbJ7QaiQ0+hAJA5RKxfi3yN\n" +
        "    3spoxt7DxW2IHXnwrFcgytMGLXO2p/RSTfgHn8voQkeJVseKL+4/iz4eQrKoif7J+KQLw7Zal1YO\n" +
        "    cT4P6tskf/eEmdEdwWYIttKEaQh1rPpFZ5DX9rOMYaUqY4dxlIWtWdJVDUb4IneaGxD8EY/0ZwZ2\n" +
        "    t/wOhKgnu5sPNH12S5x7CZocDG9fquu8klbJNc0n8cZB/FrJiI25RMLTvyEqTuQuSG2CgsNO8OIf\n" +
        "    pjyqFQHO77zFLadwj8THbZDCCQ7PIYD5eTioLIVCNvNHkyWk1qvLJI0f8YLdEQKBgQDfBRB0weMZ\n" +
        "    0/Kyis3913lBBtn0L3NA0JYWmRhtyuLdgGOxfYFxqK0fV7Im5hfL3lPZC6nUhxsU8leCk0Kpe4io\n" +
        "    n6V1405+Kd6LPAlrG9aGc9uWarx5eGqENoCJp/DxZooN3jEMga2Ov57Q29zDxycmZYu7k6Iggor9\n" +
        "    /gLxmEauRwKBgQDBz6wkfU9kaeVDAbKLGJUkbgi3XoDd83OqXMXcWxgzNMMUnWJkPDkWIne6rrbx\n" +
        "    1iQ0I56Q7Ae0nRXUQGPqhAtQP1vGkKk+K+t7IBEVhq7I8w3NoLLE8gzI7w/pd3OmI6FM2qcY6qKD\n" +
        "    Dc9WM94glHDhwQhPk2DYli97o9CwYeIoMwKBgQCq4S99tr2yoYJ5KP+yuvHuUDiZ7+2IQQpvIDjm\n" +
        "    jykK/P/mFDslgk+8Dy6yvWSIUb6/ND+ZVzf58cH5i6ntQHkycriNvm/7HUumNYcl6XuTI5yiAQCO\n" +
        "    tiQvT69nSiXEYYPwIkpEwevgz5P5hiz+WU73DaFmRTVZYYzsFj/dK4ckqwKBgEvSWg8gcvner9Fq\n" +
        "    DZZ7NIW/AmvybcnnH14L02Bkxu/peVRMShzarp6pN1vyVCICW8GGGdeqb01KkyqpUi2CIQbw8o+Z\n" +
        "    MozfE83keqqIOULmS4UhlC6WX0gQtpHoqHbbw5PUisClspRAx9d6jTEDwbGgGgc/qgixoLvnF5KZ\n" +
        "    iKefAoGAUgEqUdEnk+YZCiLCc3oxpdjbQEdpeCe6dobu/nSkb/+GQmxb88KTlKgrzl4N/onpxhss\n" +
        "    jivxRCHL+7yi18ygy6N+0ODw7PtWmTPSfjxN/FT8yiqjYAP0PujzW0lWxglad8JxGTfWiBZO5Ptf\n" +
        "    n8qQw4+NynSjOSvptcwE2Vf2I34="
private const val PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqNe8d4WEYt3Xkt25oTUJluptFTCyQSw1\n" +
        "    wqPtaGgH3i2DI5fPRprpP9s8dW8W6WBzHVyzoiSxfU24CiBly2kZF+XLAFT20Lo+XIDrDA+3YJ+H\n" +
        "    quLAP8AmHCol8jK8gp7wAERnyYgmMEtQ+V6c0PvbWWQpRnNdXsqg55oaURspZVQHEyxW1Nl3pjPo\n" +
        "    NM3rsWWYB6C/88F5mtCFsUpicFD6DSpshEu/hb0Q8CULWQwzSIATTD7QOLiUB5FRwQiS34I7bKPo\n" +
        "    TRiA7j7lNUsHsULuj+CZIgHBkkufPI0M3CL+b4eQyvgbCKqByE3SJTrhDKUVJcFmUknVCFSgcg0r\n" +
        "    Iv3QJQIDAQAB"
class CryptoTest {
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

    @Test fun `test crypto`(){

    }

    @Test fun `test aes`() {
        val data = "Test data"
        val aesKey = cryptoManager.generateAESkey()
        if(aesKey == null){
            fail("AES may not be null")
            return
        }

        val symmEncryptedData = cryptoManager.encryptSymmetric( data, aesKey)
        val cipherData = when(symmEncryptedData){
            is CryptoManager.CryptoResult.Error -> fail("Unexpected result")
            is CryptoManager.CryptoResult.Data -> symmEncryptedData.data
        }

        val encodedAesKeyStr = aesKey.encoded?.let {
            val encode = it.encodeToBase64()
            com.abnamro.push.server.print("aesKey bytes ${it.toHexArray()}")
            String(encode, Charsets.UTF_8)
        } ?: ""

        val decrypted = cryptoManager.decryptSymmetric(cipherData, encodedAesKeyStr)
        when(decrypted){
            is CryptoManager.CryptoResult.Error -> fail("Unexpected result")
            is CryptoManager.CryptoResult.Data -> assertEquals(decrypted.data, data)
        }



    }

    @Test fun `test rsa`() {
        val data = "Testdata"
        val publicKey = PUBLIC_KEY
        val encryptedData = cryptoManager.encryptAsymmetric(data, publicKey)
        val cipherData = when(encryptedData){
            is CryptoManager.CryptoResult.Error -> fail("Unexpected result")
            is CryptoManager.CryptoResult.Data -> encryptedData.data
        }

        val privateKey = PRIVATE_KEY
        val decrypted = cryptoManager.decryptAsymmetric(cipherData, privateKey)
        when(decrypted){
            is CryptoManager.CryptoResult.Data -> assertEquals(decrypted.data, data)
            else -> fail("Unexpected result")
        }


    }

    @Test fun `test rsa large data`() {
        val byteData = ByteArray(256).apply {
            Random.nextBytes(this)
        }
        val data = String(byteData.encodeToBase64(), Charsets.UTF_8)
        val publicKey = PUBLIC_KEY
        val encryptedData = cryptoManager.encryptAsymmetric(data, publicKey)
        when(encryptedData){
            is CryptoManager.CryptoResult.Error-> assertEquals("Data must not be longer than 245 bytes",encryptedData.e.message)
            else -> fail("Unexpected result")
        }

    }

}