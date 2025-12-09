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
import com.arpanrec.aphrodite.exceptions.GPGException;
import com.arpanrec.aphrodite.services.EncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@Tag(name = "Initialization", description = "Operations related to initializing the application.")
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/" + ApplicationConstants.INIT_ENDPOINT)
public class InitializationApi {

    private final EncryptionService encryptionService;

    public InitializationApi(@Autowired final EncryptionService encryptionService) {
        log.info("Initializing API");
        this.encryptionService = encryptionService;
    }

    @Operation(
            summary = "Initialize the application",
            description = "Initialize the application with the provided details",
            tags = {"Initialization"},
            requestBody =
                    @io.swagger.v3.oas.annotations.parameters.RequestBody(
                            description = "Details required to initialize the application",
                            required = true))
    @PostMapping(
            path = "/init",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public EncryptionService.UnlockKey init(@RequestBody EncryptionService.InitializeDetails initializeDetails) {
        try {
            return encryptionService.initializeApplication(initializeDetails);
        } catch (GPGException e) {
            log.error("Failed to initialize application", e);
            return null;
        }
    }

    @PostMapping(
            path = "/unlock",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void unlock(@RequestBody EncryptionService.UnlockKey unlockKey) {
        encryptionService.loadMasterEncryptionKey(unlockKey);
    }
}
