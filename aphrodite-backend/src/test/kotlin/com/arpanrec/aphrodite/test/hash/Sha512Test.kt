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

import com.arpanrec.aphrodite.hash.Sha512
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Sha512Test {
    val data = "Hello World"
    val dataHash =
        "2c74fd17edafd80e8447b0d46741ee243b7eb74dd2149a0ab1b9246fb30382f27e853d8585719e0e67cbda0daa8f51671064615d645ae27acb15bfb1447f459b"

    @Test
    fun testEncode() {
        val encodedData = Sha512.encode(data)
        assertEquals(dataHash, encodedData)
    }
}
