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
package com.arpanrec.aphrodite.api;

import com.arpanrec.aphrodite.ApplicationConstants;
import com.arpanrec.aphrodite.auth.AuthenticationImpl;
import com.arpanrec.aphrodite.models.ApiKey;
import com.arpanrec.aphrodite.services.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/token")
@Tag(name = "Token Management", description = "Operations related to managing tokens in the system.")
public class TokensApi {

    private final ApiKeyService apiKeyService;

    public TokensApi(@Autowired ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)},
            summary = "Validate token",
            operationId = "validateToken")
    @GetMapping(
            path = "/validate",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ApiKey validateToken() {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        log.info("Validating token for {}", auth.getName());
        return auth.getApiKey();
    }

    @Operation(
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)},
            summary = "Refresh token",
            operationId = "refreshToken")
    @PostMapping(
            path = "/refresh",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiKey refreshToken(@RequestBody ApiKeyRefreshRequest apiKeyRefreshRequest) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        log.info("Refreshing token for {}", auth.getName());

        ApiKey apiKey = auth.getApiKey();

        long currentExpiration = apiKey.getExpireAt();
        long newExpiration = currentExpiration + apiKeyRefreshRequest.validitySeconds();

        if (newExpiration > System.currentTimeMillis() + ApplicationConstants.MAX_API_KEY_MILI_SECONDS) {
            throw new RuntimeException("API key validity cannot exceed " + ApplicationConstants.MAX_API_KEY_MILI_SECONDS
                    + " milliseconds");
        }

        apiKey.setExpireAt(newExpiration);
        return apiKeyService.save(apiKey);
    }

    public record ApiKeyRefreshRequest(long validitySeconds) {}

    @Operation(
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)},
            summary = "Invalidate token",
            operationId = "invalidateToken")
    @PostMapping(path = "/invalidate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void invalidateToken() {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        log.info("Invalidating token for {}", auth.getName());
        ApiKey apiKey = auth.getApiKey();
        apiKeyService.delete(apiKey);
        auth.setAuthenticated(false);
    }
}
