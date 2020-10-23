package com.abnamro.push.crypto


import com.abnamro.push.common.decodeToBase64
import com.abnamro.push.common.encodeToBase64
import com.abnamro.push.crypto.util.readImage

import com.abnamro.push.crypto.util.toBitmap
import org.junit.Test

import java.io.File
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import kotlin.test.assertEquals


private const val SYM_ALGORITHM = "AES"
private const val AES_KEY_SIZE = 128

private const val TRANSACTION = "AES/ECB/PKCS5Padding"
class CryptoEcbTest {
    private val secretKey by lazy {
        val keyGen = KeyGenerator.getInstance(SYM_ALGORITHM)
        keyGen.init(AES_KEY_SIZE)
        val secretKey = keyGen.generateKey()
        secretKey
    }

    @Test
    fun `text encryption`(){
        val plainText = "Hi there"
        val bytes = plainText.toByteArray()

        val cipher = Cipher.getInstance(TRANSACTION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        val data = cipher.doFinal(bytes)
        val cipherMessage = String(data.encodeToBase64(), Charsets.UTF_8)

        checkEcb(cipherMessage, plainText)
    }

    private fun checkEcb(cipherMessage: String, expectedMessage: String) {
        val cipher = Cipher.getInstance(TRANSACTION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        val data = cipher.doFinal(cipherMessage.decodeToBase64())
        val plainMessage = String(data, Charsets.UTF_8)
        assertEquals(expectedMessage, plainMessage)
    }

    @Test
    fun `image encryption`(){
        val dataImage =  File("./src/test/res/ecb/input.bmp").readImage()

        val cipher = Cipher.getInstance(TRANSACTION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        val data = cipher.doFinal(dataImage.data)
        val imageByte = data.copyOfRange(0, dataImage.data.size)
        println("imageByte.size ${imageByte.size}")
        imageByte.toBitmap(dataImage,"./src/test/res/ecb/output.bmp")


    }
}