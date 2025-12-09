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
package com.arpanrec.aphrodite.utils

import java.security.SecureRandom

object PasswordGenerator {
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    /**
     * Generates a secure password.
     *
     * @param length Total length of the password (recommended min 12).
     * @param includeUpper Include uppercase letters.
     * @param includeLower Include lowercase letters.
     * @param includeDigits Include numbers.
     * @param includeSymbols Include special characters.
     */
    fun generate(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true,
    ): String {
        require(length >= 4) { "Password length must be at least 4 characters." }

        val charPool = StringBuilder()
        val password = StringBuilder()

        if (includeUpper) {
            charPool.append(UPPERCASE)
            password.append(UPPERCASE[secureRandom.nextInt(UPPERCASE.length)])
        }
        if (includeLower) {
            charPool.append(LOWERCASE)
            password.append(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        }
        if (includeDigits) {
            charPool.append(DIGITS)
            password.append(DIGITS[secureRandom.nextInt(DIGITS.length)])
        }
        if (includeSymbols) {
            charPool.append(SYMBOLS)
            password.append(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])
        }

        if (charPool.isEmpty()) {
            throw IllegalArgumentException("At least one character set must be selected.")
        }

        val combinedPool = charPool.toString()
        repeat(length - password.length) {
            val randomIndex = secureRandom.nextInt(combinedPool.length)
            password.append(combinedPool[randomIndex])
        }

        val shuffledPassword =
            password
                .toString()
                .toCharArray()
                .toList()
                .shuffled(secureRandom)

        return shuffledPassword.joinToString("")
    }
}
