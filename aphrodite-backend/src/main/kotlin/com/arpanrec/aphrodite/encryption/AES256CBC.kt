/*

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
                   Version 2, December 2004

Copyright (C) 2025 Arpan Mandal <me@arpanrec.com>

Everyone is permitted to copy and distribute verbatim or modified
copies of this license document, and changing it is allowed as long
as the name is changed.

           DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE
  TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION

 0. You just DO WHAT THE FUCK YOU WANT TO.

*/
package com.arpanrec.aphrodite.encryption

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES256CBC(
    keyIv: Pair<ByteArray, ByteArray>,
) {
    private val log: Logger = LogManager.getLogger(this::class.java)
    private val cipher: Cipher
    var iv: IvParameterSpec
    var key: SecretKeySpec

    init {
        log.info("Adding BouncyCastle provider, for PKCS7Padding.")
        this.cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC")
        this.key = SecretKeySpec(keyIv.first, 0, keyIv.first.size, "AES")
        this.iv = IvParameterSpec(keyIv.second)
    }

    fun encrypt(data: ByteArray): ByteArray {
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(data)
    }

    fun decrypt(cipherData: ByteArray): ByteArray {
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(cipherData)
    }

    companion object {
        fun generateKey(): Pair<ByteArray, ByteArray> {
            val keyGen: KeyGenerator = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val key: SecretKey = keyGen.generateKey()
            val randomBytes = ByteArray(16)
            SecureRandom().nextBytes(randomBytes)
            val iv = IvParameterSpec(randomBytes)
            return Pair(key.encoded, iv.iv)
        }

        fun serializeKeyIvPair(keyIvPair: Pair<ByteArray, ByteArray>): String = Json.encodeToString(keyIvPair)

        fun deserializeKeyIvPair(serializedPair: String): Pair<ByteArray, ByteArray> =
            Json.decodeFromString<Pair<ByteArray, ByteArray>>(serializedPair)
    }
}
