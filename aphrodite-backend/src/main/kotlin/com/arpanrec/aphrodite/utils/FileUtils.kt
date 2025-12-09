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

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Files
import java.nio.file.Paths

object FileUtils {
    private val log: Logger = LogManager.getLogger(this::class.java)

    fun fileOrString(pathOrString: String): String {
        if (pathOrString.isBlank()) {
            return pathOrString
        }
        try {
            val path = Paths.get(pathOrString)
            if (Files.exists(path) && path.toFile().isFile()) {
                log.debug("Loading key from file.")
                return try {
                    Files.readString(path)
                } catch (_: Exception) {
                    pathOrString
                }
            } else {
                log.debug("Loading key from string.")
                return pathOrString
            }
        } catch (_: Exception) {
            return pathOrString
        }
    }
}
