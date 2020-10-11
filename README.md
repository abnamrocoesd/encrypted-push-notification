# Push notification
This example code shows how to encrypt a push notification content and send it to a device.
It uses RSA and AES encryption algorithm to encrypt the data.

With AES the data is encrypted and with RSA publickey the AES is encrypted and send to the devices.
The client receives the encrypted data along with encrypted AES key. Since the client has the RSA privatekey, it can decrypt the AES key.
Thereafter, it uses the AES key to decrypt the data and shows the content to the user.

# Steps
1. Server knows the RSA publickey and push notification token
2. Server generates an AES key
``` kotlin
val aesKey = cryptoManager.generateAESkey()
```
3. Server encrypts the data with AES key (`CryptoManager.decryptSymmetric`)
``` kotlin
val payloadEncrypted = cryptoManager.encryptSymmetric(payloadStr, aesKey)
```
4. Server encrypts the AES key with RSA publickey (`CryptoManager.decryptAsymmetric`)
``` kotlin
val encryptedAesKey = cryptoManager.encryptAsymmetric(encodedAesKeyStr, publicKey)
```
5. Server sends message to device with push notification token

CryptoManager can also be used to decrypt the content in Android

# Data structure

You can use your own data set. In this example we use the following data
``` json
{
  "encrypted-content": {
    "version": "string",
    "title": "fallback title",
    "message": "fallback message",
    "type": "notification type (if needed)",
    "key": "encrypted symmetric key (AES). This key is encrypted with the asymmetic publickey",
    "payload": "encrypted push message data. This is encrypted using the symmetic AES key"
  }
}

//Data structure of encrypted content
{
  "title": "encrypted title",
  "message": "encrypted message",
  "type": "notification type (if needed)",
  "url": "Deeplink which describes what to do after the notification is opend"
}
```
