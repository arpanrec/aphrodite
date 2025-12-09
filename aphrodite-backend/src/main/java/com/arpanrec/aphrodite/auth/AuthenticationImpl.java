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

import com.arpanrec.aphrodite.ApplicationConstants;
import com.arpanrec.aphrodite.exceptions.NotInitializedException;
import com.arpanrec.aphrodite.models.AccessLog;
import com.arpanrec.aphrodite.models.ApiKey;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.Role;
import com.arpanrec.aphrodite.services.ApiKeyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.security.auth.Subject;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@Log4j2
@Data
public class AuthenticationImpl implements Authentication {

    @Serial
    private static final long serialVersionUID = -8620294545092862085L;

    private boolean authenticated = false;

    private AccessLog accessLog;

    private ApiKey apiKey;

    private Namespace namespace;

    public void loadAuthentication(final ApiKeyService apiKeyService) {
        String authString =
                accessLog.findHere(ApplicationConstants.API_KEY_HEADER).orElse(null);

        if (authString == null || authString.isBlank()) {
            authString = accessLog
                    .findHere(ApplicationConstants.AUTHORIZATION_HEADER)
                    .orElse(null);
        }

        if (authString == null || authString.isBlank()) {
            return;
        }

        if (authString.toLowerCase().startsWith("bearer ")) {
            authString = authString.substring(7);
        } else if (authString.toLowerCase().startsWith("basic ")) {
            var basicAuthString = authString.substring(6);
            try {
                var decodedAuthString = new String(java.util.Base64.getDecoder().decode(basicAuthString));
                var authParts = decodedAuthString.split(":");
                authString = authParts[1];
            } catch (IllegalArgumentException e) {
                log.debug("Failed to decode basic auth string: {}", e.getMessage());
                return;
            }
        } else {
            log.debug("Unsupported authentication type, assuming JWT");
        }

        Jws<Claims> parsedJwt = Jwts.parser()
                .keyLocator(_ -> {
                    try {
                        return apiKeyService.getJwtSecretKey(namespace);
                    } catch (NotInitializedException e) {
                        throw new RuntimeException("Unable to retrieve JWT secret key", e);
                    }
                })
                .build()
                .parseSignedClaims(authString);
        this.apiKey = apiKeyService.findByIdAndNamespace(
                UUID.fromString(parsedJwt.getHeader().getKeyId()), namespace);
        this.apiKey = apiKeyService.setLastUsedAt(this.apiKey);
    }

    @Override
    public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
        if (apiKey == null) {
            return new ArrayList<>();
        }
        return apiKey.getUser().getAuthorities();
    }

    @Override
    public Object getCredentials() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getPassword();
    }

    @Override
    public Object getDetails() {
        if (apiKey == null) {
            return null;
        }
        return apiKey.getUser();
    }

    @Override
    public Object getPrincipal() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        if (apiKey == null) {
            return null;
        }
        return this.apiKey.getUser().getUsername();
    }

    @Override
    public boolean implies(Subject subject) {
        return Authentication.super.implies(subject);
    }

    public Pair<@NotNull String, @NotNull Boolean> getIfAllowedOnUriAndMethod() {
        var roles = apiKey.getUser().getRoles();
        for (Role role : roles) {
            Pattern pathPattern;
            try {
                pathPattern = Pattern.compile(role.getRolePrivUri());
            } catch (PatternSyntaxException e) {
                log.error("Invalid regex pattern for path privilege: {}", role.getRolePrivUri(), e);
                continue;
            }
            if (pathPattern.matcher(this.accessLog.getRequestUri()).matches()) {
                Set<String> deniedMethods = role.getRolePrivUriDeniedMethods();
                for (String deniedMethod : deniedMethods) {
                    if (this.accessLog.getMethod().equalsIgnoreCase(deniedMethod)) {
                        return Pair.of(
                                "Denied by path privilege: "
                                        + role.getRolePrivUri()
                                        + " for method: "
                                        + this.accessLog.getMethod(),
                                false);
                    }
                }
                Set<String> allowedMethods = role.getRolePrivUriAllowedMethods();
                for (String allowedMethod : allowedMethods) {
                    if (this.accessLog.getMethod().equalsIgnoreCase(allowedMethod)) {
                        return Pair.of(
                                "Allowed by path privilege: "
                                        + role.getRolePrivUri()
                                        + " for method: "
                                        + this.accessLog.getMethod(),
                                true);
                    }
                }
            }
        }
        return Pair.of("No allowed or denied path privileges found", false);
    }
}
