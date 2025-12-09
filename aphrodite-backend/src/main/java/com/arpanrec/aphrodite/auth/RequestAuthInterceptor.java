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
import com.arpanrec.aphrodite.exceptions.NameSpaceNotFoundException;
import com.arpanrec.aphrodite.models.AccessLog;
import com.arpanrec.aphrodite.services.AccessLogRepository;
import com.arpanrec.aphrodite.services.NamespaceService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Log4j2
@Component
public class RequestAuthInterceptor extends OncePerRequestFilter {

    private final NamespaceService nameSpaceService;
    private final AuthenticationManager authenticationManager;
    private final AccessLogRepository accessLogRepository;

    public RequestAuthInterceptor(
            @Autowired AuthenticationManagerImpl authenticationManagerImpl,
            @Autowired AccessLogRepository accessLogRepository,
            @Autowired NamespaceService nameSpaceService) {
        this.authenticationManager = authenticationManagerImpl;
        this.accessLogRepository = accessLogRepository;
        this.nameSpaceService = nameSpaceService;
    }

    protected void doFilterInternal(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        log.info(
                "Authenticating request from: {} to {} {}",
                request.getRemoteAddr(),
                request.getMethod(),
                request.getRequestURI());
        AccessLog accessLog = new AccessLog(request);
        accessLogRepository.save(accessLog);
        AuthenticationImpl authentication = new AuthenticationImpl();
        String namespaceFromReq =
                accessLog.findHere(ApplicationConstants.NAMESPACE_HEADER).orElse(null);
        if (namespaceFromReq != null && !namespaceFromReq.isBlank()) {
            authentication.setNamespace(nameSpaceService
                    .getOptional(namespaceFromReq)
                    .orElseThrow(() -> new NameSpaceNotFoundException("Namespace not found: " + namespaceFromReq)));
        }
        authentication.setAccessLog(accessLog);
        Authentication authenticated = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        filterChain.doFilter(request, response);
    }
}
