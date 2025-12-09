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
package com.arpanrec.aphrodite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@ConfigurationProperties(prefix = "aphrodite")
@Data
public class ApplicationConfigurations {

    /** A list of keys that can be used to unlock the application. */
    private List<String> unlockKeys = new ArrayList<>();

    /**
     * A mapping of namespace names to their corresponding root user passwords.
     *
     * <p>This map is used to configure and manage root user credentials for different namespaces. Each key in the map
     * represents the name of a namespace, and the associated value is the respective root user's password for that
     * namespace.
     *
     * <p>Example usage:
     *
     * <pre>{@code
     * // Accessing the root password for a specific namespace
     * String rootPassword = rootPasswords.get("namespaceName");
     * }</pre>
     *
     * <p>Note: If a password is null or not provided, a random password will be generated and appropriately logged.
     *
     * @see ApplicationConfigurations
     */
    private Map<String, String> rootPasswords = new HashMap<>();
}
