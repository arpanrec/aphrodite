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

import com.arpanrec.aphrodite.exceptions.HashingException
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.MessageDigest
import kotlin.text.Charsets

object Sha512 : PasswordEncoder {
    override fun encode(rawPassword: CharSequence?): String {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw HashingException("Password cannot be blank.")
        }
        return encode(rawPassword.toString().toByteArray(Charsets.UTF_8))
    }

    fun encode(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-512")
        val hash = digest.digest(data)
        return hash.toHexString()
    }

    override fun matches(
        rawPassword: CharSequence?,
        encodedPassword: String?,
    ): Boolean {
        if (rawPassword == null || rawPassword.isBlank() || encodedPassword == null || encodedPassword.isBlank()) {
            throw HashingException("Password cannot be blank.")
        }
        return encode(rawPassword) == encodedPassword
    }
}
