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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/key-value/metadata/{bucket}")
@Tag(name = "KeyValueMetadata", description = "Operations related to managing kv metadata in the system.")
public class KeyValueMetaDataApi {

    private final KeyValueService keyValueService;

    public KeyValueMetaDataApi(@Autowired KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @Operation(
            parameters = {
                @Parameter(
                        name = "key",
                        in = ParameterIn.PATH,
                        description = "Secret Key Path",
                        example = "app/config/db-password"),
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(
            path = "/list-keys/{*key}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public List<String> listKeyValueKeys(@PathVariable String bucket, @PathVariable String key) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        log.info("Listing secret {}", key);
        assert auth != null;
        return keyValueService.list(key, bucket, auth.getNamespace());
    }

    @Operation(
            parameters = {
                @Parameter(
                        name = "key",
                        in = ParameterIn.PATH,
                        description = "Secret Key Path",
                        example = "app/config/db-password"),
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(
            path = "/details/{*key}",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public KeyValueService.KeyValueMetaData getKeyValueMetaData(@PathVariable String bucket, @PathVariable String key) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        log.info("Reading secret metadata {}", key);
        assert auth != null;
        return keyValueService.getMetaData(key, bucket, auth.getNamespace());
    }
}
