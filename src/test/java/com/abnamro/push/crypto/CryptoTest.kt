package com.abnamro.push.crypto

import com.abnamro.push.common.CryptoManager
import com.abnamro.push.common.LogBridge
import com.abnamro.push.common.SecureRandomBridge
import com.abnamro.push.common.encodeToBase64
import com.abnamro.push.server.notifier.toHexArray
import org.junit.Test
import java.security.SecureRandom
import kotlin.test.assertEquals
import kotlin.test.fail

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


}