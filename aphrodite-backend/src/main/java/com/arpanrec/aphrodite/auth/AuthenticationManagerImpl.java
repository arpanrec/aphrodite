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
package com.arpanrec.aphrodite.auth;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationManagerImpl implements AuthenticationManager {

    private final AuthenticationProvider authenticationProvider;

    public AuthenticationManagerImpl(@Autowired AuthenticationProviderImpl authenticationProviderImpl) {
        this.authenticationProvider = authenticationProviderImpl;
    }

    @Override
    public @NotNull Authentication authenticate(@NotNull Authentication authentication) {
        return Objects.requireNonNull(authenticationProvider.authenticate(authentication));
    }
}
