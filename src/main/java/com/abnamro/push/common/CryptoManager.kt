package com.abnamro.push.crypto


import java.nio.ByteBuffer
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec


private const val SYM_ALGORITHM = "AES"
private const val SYM_TRANSFORMATION = "AES/GCM/NoPadding"
private const val ASYM_ALGORITHM = "RSA"
private const val ASYM_TRANSFORMATION = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING"
private const val AES_KEY_SIZE = 128
private const val GCM_NONCE_LENGTH = 12

interface CryptoManager {
    fun decryptSymmetric(strData: String, privateKey: String): CryptoResult
    fun decryptAsymmetric(strData: String, privateKey: String): CryptoResult
    fun generateAESkey(): SecretKey?

    sealed class CryptoResult{
        class Error(val e: Throwable): CryptoResult()
        class Data(val data: String): CryptoResult()

    }
    class Impl(private val logger: LogBridge, private val random: SecureRandomBridge): CryptoManager{
        //It is also called IV. The is just a random number to harden crypto and it is required by GCM
        private val aesNonce by lazy{
            ByteArray(GCM_NONCE_LENGTH).apply {
                random.nextBytes(this)
            }
        }

        override fun decryptAsymmetric(strData: String, privateKey: String): CryptoResult {
            val res = try {
                val data: ByteArray = strData.decodeToBase64()
                logger.debug(message = "data size: ${data.size}")

                val keySpec = PKCS8EncodedKeySpec(privateKey.decodeToBase64())
                val keyFactory = KeyFactory.getInstance(ASYM_ALGORITHM)
                val privateKeyObj = keyFactory.generatePrivate(keySpec)

                val cipher = Cipher.getInstance(ASYM_TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, privateKeyObj)
                val decryptedByte = cipher.doFinal(data)

                CryptoResult.Data(String(decryptedByte.encodeToBase64(), Charsets.UTF_8))
            } catch (e: java.lang.Exception) { //NOSONAR
                logger.debug("Failed to decrypt: ", e)
                CryptoResult.Error(e)
            }

            return res
        }

        fun encryptAsymmetric(dataStr: String, publicKey: String) =  try {
                val data = dataStr.decodeToBase64()
                val keySpec = X509EncodedKeySpec(publicKey.decodeToBase64())
                val keyFactory = KeyFactory.getInstance(ASYM_ALGORITHM)
                val publicKeyObj = keyFactory.generatePublic(keySpec)

                val cipher = Cipher.getInstance(ASYM_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, publicKeyObj)
                val encData = cipher.doFinal(data)

                logger.debug("aesKey encrypted size ${encData.size}")
                CryptoResult.Data( String(encData.encodeToBase64(), Charsets.UTF_8))
            } catch (e: Exception) {//NOSONAR
                logger.error(message =  "Failed to decrypt: ", e=e)
                CryptoResult.Error(e)
            }



        override fun generateAESkey(): SecretKey? {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(AES_KEY_SIZE, random.getImpl())
            val secretKey = keyGen.generateKey()
            return secretKey
        }

        override fun decryptSymmetric(strData: String, privateKey: String)= try {
                val cipherMessage = strData.decodeToBase64()
                logger.debug("AES decrypting cipherMessage size ${cipherMessage.size}")

                //use first GCM_NONCE_LENGTH bytes for nonce
                val gcmNonce = GCMParameterSpec(AES_KEY_SIZE, cipherMessage, 0, GCM_NONCE_LENGTH)
                val cipher = Cipher.getInstance(SYM_TRANSFORMATION)
                val keySpec = SecretKeySpec(privateKey.decodeToBase64(), SYM_ALGORITHM)

                cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmNonce)

                //use everything from GCM_NONCE_LENGTH bytes on as ciphertext
                val plainText = cipher.doFinal(cipherMessage, GCM_NONCE_LENGTH, cipherMessage.size - GCM_NONCE_LENGTH)

                CryptoResult.Data( String(plainText, Charsets.UTF_8))
            } catch (e: Exception) { //NOSONAR
                logger.error(message =  "Failed to decrypt: ", e = e)
                CryptoResult.Error(e)
            }



        fun encryptSymmetric(message: String, aesKey: SecretKey): CryptoResult {
            val data = message.toByteArray()

            // Encrypt cipher
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val parameterSpec = GCMParameterSpec(AES_KEY_SIZE, aesNonce) //128 bit auth tag length
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec)
            val cipherText = cipher.doFinal(data)

            logger.debug("AES cipherText size ${cipherText.size}")

            //Now we have to add nonce to the same cipherData and send it over the line
            //Nonce will be the first GCM_NONCE_LENGTH byte and the rest will be cipherText
            val byteBuffer = ByteBuffer.allocate(aesNonce.size + cipherText.size)
            byteBuffer.put(aesNonce)
            byteBuffer.put(cipherText)
            val cipherData = byteBuffer.array()
            logger.debug("AES cipherData size ${cipherData.size}")

            val cipherMessage = String(cipherData.encodeToBase64(), Charsets.UTF_8)
            logger.debug("AES cipherMessage $cipherMessage")
            return CryptoResult.Data( cipherMessage)
        }
    }
}

fun String.decodeToBase64() = org.apache.commons.codec.binary.Base64.decodeBase64(this)
fun ByteArray.encodeToBase64() = org.apache.commons.codec.binary.Base64.encodeBase64(this)

interface SecureRandomBridge{
    fun nextBytes(b: ByteArray)
    fun getImpl(): SecureRandom
}
interface  LogBridge{
    fun debug(message: String?, e: Throwable?=null)
    fun error(message: String?, e: Throwable?=null)
}