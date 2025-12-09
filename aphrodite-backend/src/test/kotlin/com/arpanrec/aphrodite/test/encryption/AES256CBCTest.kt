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
package com.arpanrec.aphrodite.test.encryption

import com.arpanrec.aphrodite.encryption.AES256CBC
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Test
import java.util.Base64

class AES256CBCTest {
    private val log: Logger = LogManager.getLogger(this::class.java)

    private val data = "Hello, World!"
    private val cipherDataB64 = "yQp5HF92QfpV/jdmPIDYJQ=="
    val keyB64 = "5jcK7IMk3+QbNLikFRl3Zwgl9xagKD87s5dT2UqaSR4=" // gitleaks:allow
    val ivB64 = "5jcK7IMk3+QbNLikFRl3Zw==" // gitleaks:allow
    private val aes256cbc = AES256CBC(Pair(Base64.getDecoder().decode(keyB64), Base64.getDecoder().decode(ivB64)))

    @Test
    fun testEncrypt() {
        log.info("Encrypting plain text")
        val newCipherData = aes256cbc.encrypt(data.toByteArray())
        val newCipherDataB64 = Base64.getEncoder().encodeToString(newCipherData)
        log.info("New Cipher Data Base64: $newCipherDataB64")
        assert(cipherDataB64 == newCipherDataB64) { "Cipher text does not match" }
    }

    @Test
    fun testDecrypt() {
        log.info("Decrypting cipher text")
        val cipherData = Base64.getDecoder().decode(cipherDataB64)
        val decryptedData = aes256cbc.decrypt(cipherData)
        val newData = String(decryptedData)
        log.info("Decrypted text: $newData")
        assert(newData == data) { "Decrypted text does not match" }
    }
}
