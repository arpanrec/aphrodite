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
package com.arpanrec.aphrodite.hash

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.SecureRandom
import kotlin.text.toByteArray

object Argon2 : PasswordEncoder {
    private const val SALT_LENGTH = 32

    private fun generateSalt(): ByteArray {
        val secureRandom = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        secureRandom.nextBytes(salt)
        return salt
    }

    private const val CHARACTERS = "abcdefghijklmnopqrstuvwxyz"

    private fun hashString(
        rawPassword: ByteArray,
        salt: ByteArray,
    ): ByteArray {
        val random = SecureRandom()
        val randomIndex = random.nextInt(CHARACTERS.length)
        val randomChar = CHARACTERS[randomIndex].toString().toByteArray()
        return hashString(rawPassword, salt, randomChar)
    }

    private fun hashString(
        rawPassword: ByteArray,
        salt: ByteArray,
        paper: ByteArray,
    ): ByteArray {
        val inputStringWithPepper: ByteArray = rawPassword + paper
        val iterations = 2
        val memLimit = 66536
        val hashLength = 32
        val parallelism = 1
        val builder =
            Argon2Parameters
                .Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iterations)
                .withMemoryAsKB(memLimit)
                .withParallelism(parallelism)
                .withSalt(salt)

        val generate = Argon2BytesGenerator()
        generate.init(builder.build())
        val result = ByteArray(hashLength)
        generate.generateBytes(inputStringWithPepper, result, 0, result.size)
        return result
    }

    override fun encode(rawPassword: CharSequence?): String {
        val salt = generateSalt()
        val hashedPassword = hashString(rawPassword.toString().toByteArray(), salt)
        val saltHashedPassword = salt.toHexString() + ":" + hashedPassword.toHexString()
        return saltHashedPassword
    }

    override fun matches(
        rawPassword: CharSequence?,
        encodedPassword: String?,
    ): Boolean {
        if (rawPassword == null || encodedPassword == null || encodedPassword.isEmpty() || rawPassword.isEmpty()) {
            return false
        }
        val encodedPasswordParts = encodedPassword.split(":")
        if (encodedPasswordParts.size != 2) {
            return false
        }
        val salt = encodedPasswordParts[0].hexToByteArray()
        val hashedPassword = encodedPasswordParts[1].hexToByteArray()
        for (c in CHARACTERS) {
            val tryHashPassword = hashString(rawPassword.toString().toByteArray(), salt, c.toString().toByteArray())
            if (tryHashPassword.contentEquals(hashedPassword)) {
                return true
            }
        }
        return false
    }
}
