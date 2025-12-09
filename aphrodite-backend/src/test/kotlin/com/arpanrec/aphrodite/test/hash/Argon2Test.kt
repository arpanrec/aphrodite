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
package com.arpanrec.aphrodite.test.hash

import com.arpanrec.aphrodite.hash.Argon2
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.jupiter.api.Test

class Argon2Test {
    private val log: Logger = LogManager.getLogger(this::class.java)
    private val password = "root1"
    private val hashedPassword =
        "0889586e3233b90e0aa6b974fb71e9102f97249652b672424be9b84a533239bb:bc7394da3d00a2832d063ab425d6f2fac7d33d08761d4e51ea9683d7e49f7e42"

    @Test
    fun testHashedPassword() {
        log.info("OLD Encoded Password: $hashedPassword")
        assert(Argon2.matches(password, hashedPassword)) { "Old hashed password does not match" }
        log.info("Old encoded password matches")
    }

    @Test
    fun testNewHashedPassword() {
        val newEncodedPassword = Argon2.encode(this.password)
        log.info("New Hashed Password: $newEncodedPassword")
        assert(Argon2.matches(password, newEncodedPassword)) { "New encoded password does not match" }
        log.info("New hashed password matches")
    }

    @Test
    fun testNewHash() {
        val newHash = Argon2.encode(this.password)
        log.info("New Hash: $newHash")
        assert(newHash != hashedPassword) { "New hash matches old hash" }
        log.info("New hash does not match old hash")
    }

    @Test
    fun wrongPasswordTest() {
        val newEncodedPassword = Argon2.encode(password + "1")
        assert(!Argon2.matches(password, newEncodedPassword)) { "Wrong password matches" }
        log.info("Wrong password does not match")
    }
}
