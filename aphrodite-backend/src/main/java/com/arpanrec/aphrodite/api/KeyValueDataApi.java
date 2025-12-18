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
import com.arpanrec.aphrodite.services.KeyValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/key-value/data/{bucket}")
@Tag(name = "KeyValueData", description = "Operations related to managing kv in the system.")
public class KeyValueDataApi {

    private final KeyValueService keyValueService;

    public KeyValueDataApi(@Autowired KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @Operation(
            parameters = {
                @Parameter(
                        name = "key",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "Secret Key Path",
                        example = "app/config/db-password"),
                @Parameter(
                        name = "version",
                        in = ParameterIn.QUERY,
                        description = "Secret Key Version",
                        allowEmptyValue = true,
                        example = "1")
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(
            path = "/{*key}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> readKeyValueData(
            @PathVariable String bucket,
            @PathVariable String key,
            @RequestParam(name = "version", required = false, defaultValue = "0") int version) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        log.info("Reading secret {}", key);
        assert auth != null;
        return keyValueService.get(key, version, bucket, auth.getNamespace());
    }

    @Operation(
            parameters = {
                @Parameter(
                        name = "key",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "Secret Key Path",
                        example = "app/config/db-password"),
                @Parameter(
                        name = "version",
                        in = ParameterIn.QUERY,
                        description = "Secret Key Version",
                        allowEmptyValue = true,
                        example = "1"),
            },
            requestBody =
                    @RequestBody(
                            required = true,
                            description = "Key-value secret data",
                            content =
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(type = "object"))),
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PutMapping(
            path = "/{*key}",
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public WriteKeyValueDataResponse writeKeyValueData(
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> body,
            @PathVariable String bucket,
            @PathVariable String key,
            @RequestParam(name = "version", required = false, defaultValue = "0") int version) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        log.info("Writing secret: {}", key);
        assert auth != null;
        int versionCreated = keyValueService.save(key, body, version, bucket, auth.getNamespace());
        return new WriteKeyValueDataResponse(versionCreated);
    }

    public record WriteKeyValueDataResponse(int version) {}

    @Operation(
            parameters = {
                @Parameter(
                        name = "key",
                        in = ParameterIn.PATH,
                        required = true,
                        description = "Secret Key Path",
                        example = "app/config/db-password"),
                @Parameter(
                        name = "version",
                        in = ParameterIn.QUERY,
                        description = "Secret Key Version",
                        allowEmptyValue = true,
                        example = "1")
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @DeleteMapping(path = "/{*key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteKeyValueData(
            @PathVariable String bucket,
            @PathVariable String key,
            @RequestParam(name = "version", required = false, defaultValue = "0") int version) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        log.info("Deleting secret: {}, version: {}", key, version);
        assert auth != null;
        keyValueService.delete(key, version, bucket, auth.getNamespace());
    }
}
