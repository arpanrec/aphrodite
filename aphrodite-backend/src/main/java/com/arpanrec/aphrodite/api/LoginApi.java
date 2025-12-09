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
import com.arpanrec.aphrodite.exceptions.UserNotFoundException;
import com.arpanrec.aphrodite.models.User;
import com.arpanrec.aphrodite.services.ApiKeyService;
import com.arpanrec.aphrodite.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(
        path = ApplicationConstants.API_ENDPOINT + "/" + ApplicationConstants.LOGIN_ENDPOINT,
        produces = {MediaType.APPLICATION_JSON_VALUE},
        consumes = {MediaType.APPLICATION_JSON_VALUE})
@Tag(name = "Login", description = "Operations related to logging in to the application.")
public class LoginApi {

    private final UserService userService;
    private final ApiKeyService apiKeyService;

    public LoginApi(@Autowired UserService userService, @Autowired ApiKeyService apiKeyService) {
        this.userService = userService;
        this.apiKeyService = apiKeyService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/userpass")
    public LoginResponse userpassLogin(@RequestBody UserPassLoginRequest loginRequest, HttpServletRequest request) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        User user = userService
                .findByUsernameAndNamespace(loginRequest.username, auth.getNamespace())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.isValidLoginAttemptOrError(loginRequest.password);
        String ip = request.getRemoteAddr();
        String token = apiKeyService.generateToken(
                user, ip, loginRequest.comment, loginRequest.validitySeconds, auth.getNamespace());
        return new LoginResponse(token);
    }

    public record LoginResponse(String token) {}

    public record UserPassLoginRequest(String username, String password, String comment, long validitySeconds) {}
}
