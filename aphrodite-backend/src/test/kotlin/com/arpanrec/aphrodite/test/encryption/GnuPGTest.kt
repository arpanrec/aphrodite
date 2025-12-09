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

import com.arpanrec.aphrodite.encryption.GnuPG
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.jupiter.api.Test
import java.security.Security

class GnuPGTest : GnuPGData() {
    private val encryption: GnuPG
    private val log: Logger = LogManager.getLogger(this::class.java)

    init {
        Security.addProvider(BouncyCastleProvider())
        val gnuPG = GnuPG(GnuPG.fromArmor(armoredPrivateKey), privateKeyPassphrase)
        encryption = gnuPG
    }

    @Test
    fun testEncrypt() {
        val encryptedMessage = encryption.encrypt(message.toByteArray())
        log.info("Able to encrypt message: {}", encryptedMessage)
    }

    @Test
    fun testDecrypt() {
        val decryptedMessage = encryption.decrypt(GnuPG.fromArmor(armoredBcEncryptedMessage))
        log.info("Able to decrypt message: {}", decryptedMessage)
        assert(String(decryptedMessage, Charsets.US_ASCII) == message) {
            "Decrypted message is not same as " + "original message"
        }
    }

    @Test
    fun testEncryptAndDecrypt() {
        val newEncryptedMessage = encryption.encrypt(message.toByteArray())
        val newArmoredBcEncryptedMessage = GnuPG.toArmor(newEncryptedMessage)
        log.info("Encrypted message: {}", newArmoredBcEncryptedMessage)
        val decryptedMessage = encryption.decrypt(GnuPG.fromArmor(newArmoredBcEncryptedMessage))
        log.info("Decrypted message: {}", decryptedMessage)
        assert(String(decryptedMessage, Charsets.US_ASCII) == message)
    }

    @Test
    fun testNewEncryptedMessage() {
        val newEncryptedMessage = encryption.encrypt(message.toByteArray())
        val newArmoredBcEncryptedMessage = GnuPG.toArmor(newEncryptedMessage)
        log.info("New encrypted message: {}", newArmoredBcEncryptedMessage)
        assert(!newArmoredBcEncryptedMessage.contentEquals(armoredBcEncryptedMessage)) {
            "New encrypted message is not same as old encrypted message"
        }
    }

    @Test
    fun testCliEncryptedMessage() {
        val decryptedMessage = encryption.decrypt(GnuPG.fromArmor(armoredCliEncryptedMessage))
        log.info("Decrypted CLI message: {}", decryptedMessage)
        assert(String(decryptedMessage, Charsets.US_ASCII) == message)
    }

    @Test
    fun createNewGPGKey() {
        val newKey = GnuPG.createGpgPrivateKey("arpan", "arpan.rec@gmail.com", null, 0)
        val newKeyArmor = GnuPG.toArmor(newKey)
        log.info("New : {}", newKeyArmor)
        val gnuPG = GnuPG(GnuPG.fromArmor(newKeyArmor), null)
        val encryptedData = gnuPG.encrypt(message.toByteArray())
        val armoredEncryptedData = GnuPG.toArmor(encryptedData)
        log.info("Encrypted data: {}", armoredEncryptedData)
        val decryptedData = gnuPG.decrypt(GnuPG.fromArmor(armoredEncryptedData))
        log.info("Decrypted data: {}", decryptedData)
        assert(String(decryptedData, Charsets.US_ASCII) == message) {
            "Decrypted data is not same as original data"
        }
    }

    @Test
    fun createNewGPGKeyWithPassphraseAndExpiry() {
        log.info("Starting test : createNewGPGKeyWithPassphraseAndExpiry")
        val newKey = GnuPG.createGpgPrivateKey("arpan", "arpan.rec@gmail.com", "password", 365)
        val newKeyArmor = GnuPG.toArmor(newKey)
        log.info("New with pass and exp: {}", newKeyArmor)
        val gnuPG = GnuPG(GnuPG.fromArmor(newKeyArmor), "password")
        val encryptedData = gnuPG.encrypt(message.toByteArray())
        val armoredEncryptedData = GnuPG.toArmor(encryptedData)
        log.info("Encrypted data with pass and exp: {}", armoredEncryptedData)
        val decryptedData = gnuPG.decrypt(GnuPG.fromArmor(armoredEncryptedData))
        log.info("Decrypted data with pass and exp: {}", decryptedData)
        assert(String(decryptedData, Charsets.US_ASCII) == message) {
            "Decrypted data is not same as original data"
        }
        log.info("Completed test : createNewGPGKeyWithPassphraseAndExpiry")
    }
}
