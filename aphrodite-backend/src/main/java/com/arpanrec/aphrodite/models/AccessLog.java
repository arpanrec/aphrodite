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
package com.arpanrec.aphrodite.models;

import com.arpanrec.aphrodite.attributeconverters.MapStringArrayString;
import com.arpanrec.aphrodite.attributeconverters.MapStringString;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Entity(name = "access_log_t")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccessLog {
    public AccessLog(@NotNull HttpServletRequest request) {
        this.remoteAddr = request.getRemoteAddr();
        this.time = System.currentTimeMillis();
        Enumeration<String> allHeaderNames = request.getHeaderNames();
        while (allHeaderNames.hasMoreElements()) {
            String apiHeader = allHeaderNames.nextElement();
            this.headers.put(apiHeader, request.getHeader(apiHeader));
        }
        this.method = request.getMethod();
        this.requestUri = request.getRequestURI();

        for (String parameterKey : request.getParameterMap().keySet()) {
            this.params.put(parameterKey, request.getParameterValues(parameterKey));
        }

        Cookie[] allCookies = request.getCookies();
        if (allCookies != null) {
            for (Cookie cookie : allCookies) {
                this.cookies.put(cookie.getName(), cookie.getValue());
            }
        }
        this.id = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "remote_addr_c")
    private String remoteAddr;

    @Column(name = "time_c")
    private long time;

    @NotNull
    @Column(name = "headers_c", columnDefinition = "TEXT")
    @Convert(converter = MapStringString.class)
    private Map<String, String> headers = new HashMap<>();

    @NotNull
    @Column(name = "cookies_c", columnDefinition = "TEXT")
    @Convert(converter = MapStringString.class)
    private Map<String, String> cookies = new HashMap<>();

    @Column(name = "method_c")
    private String method;

    @Column(name = "request_uri_c")
    private String requestUri;

    @NotNull
    @Column(name = "params_c")
    @Convert(converter = MapStringArrayString.class)
    private Map<String, String[]> params = new HashMap<>();

    public Optional<String> findHere(@NotNull String key) {
        var fromHeaders = headers.get(key);
        if (fromHeaders != null && !fromHeaders.isBlank()) {
            return Optional.of(fromHeaders);
        }
        var fromCookies = cookies.get(key);
        if (fromCookies != null && !fromCookies.isBlank()) {
            return Optional.of(fromCookies);
        }
        var fromParams = params.get(key);
        if (fromParams != null && fromParams.length > 0 && !fromParams[0].isBlank()) {
            return Optional.of(fromParams[0]);
        }
        return Optional.empty();
    }
}
