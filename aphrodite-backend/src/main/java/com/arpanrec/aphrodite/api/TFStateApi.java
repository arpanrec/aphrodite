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
import com.arpanrec.aphrodite.exceptions.TerraformStateConflictError;
import com.arpanrec.aphrodite.exceptions.TerraformStateLockedError;
import com.arpanrec.aphrodite.models.TerraformStateLock;
import com.arpanrec.aphrodite.services.TerraformStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/tf-state/{bucket}")
@Tag(name = "TerraformState", description = "Operations related to managing tf state in the system.")
public class TFStateApi {

    private final TerraformStateService terraformStateService;

    public TFStateApi(@Autowired TerraformStateService terraformStateService) {
        this.terraformStateService = terraformStateService;
    }

    @Operation(
            parameters = {
                @Parameter(name = "bucket", in = ParameterIn.PATH, description = "Name of the bucket", required = true),
                @Parameter(
                        name = "tfstate",
                        in = ParameterIn.PATH,
                        description = "Name of the tfstate file",
                        required = true)
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/data/{tfstate}")
    public Map<String, Object> getTfStateData(@PathVariable String bucket, @PathVariable String tfstate) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Reading tfstate {}", tfstate);
        assert auth != null;
        var state = terraformStateService.getState(tfstate, bucket, auth.getNamespace());
        if (state == null || state.isEmpty()) {
            return Map.of("version", 3);
        }
        return state;
    }

    @Operation(
            parameters = {
                @Parameter(name = "bucket", in = ParameterIn.PATH, description = "Name of the bucket", required = true),
                @Parameter(
                        name = "tfstate",
                        in = ParameterIn.PATH,
                        description = "Name of the tfstate file",
                        required = true),
                @Parameter(name = "ID", in = ParameterIn.QUERY, description = "Lock ID", allowEmptyValue = true)
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/data/{tfstate}")
    public HttpEntity<Object> setTfStateData(
            @RequestParam(name = "ID", required = false) String lockId,
            @PathVariable String bucket,
            @PathVariable String tfstate,
            @RequestBody Map<String, Object> body) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Updating tfstate {}", tfstate);
        assert auth != null;

        try {
            terraformStateService.save(tfstate, body, lockId, bucket, auth.getNamespace());
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (TerraformStateLockedError e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getTerraformStateLock());
        } catch (TerraformStateConflictError e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getTerraformStateLock());
        }
    }

    @Operation(
            parameters = {
                @Parameter(name = "bucket", in = ParameterIn.PATH, description = "Name of the bucket", required = true),
                @Parameter(
                        name = "tfstate",
                        in = ParameterIn.PATH,
                        description = "Name of the tfstate file",
                        required = true),
                @Parameter(name = "ID", in = ParameterIn.QUERY, description = "Lock ID", allowEmptyValue = true)
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/data/{tfstate}")
    public HttpEntity<Object> deleteTfStateData(
            @RequestParam(name = "ID", required = false) String lockId,
            @PathVariable String bucket,
            @PathVariable String tfstate) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Deleting tfstate Data {}", tfstate);
        assert auth != null;
        try {
            terraformStateService.markAllVersionForDelete(tfstate, lockId, bucket, auth.getNamespace());
            return ResponseEntity.ok().build();
        } catch (TerraformStateLockedError e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getTerraformStateLock());
        } catch (TerraformStateConflictError e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getTerraformStateLock());
        }
    }

    @Operation(
            parameters = {
                @Parameter(name = "bucket", in = ParameterIn.PATH, description = "Name of the bucket", required = true),
                @Parameter(
                        name = "tfstate",
                        in = ParameterIn.PATH,
                        description = "Name of the tfstate file",
                        required = true)
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/lock/{tfstate}")
    public HttpEntity<Object> acquireLock(
            @PathVariable String bucket, @PathVariable String tfstate, @RequestBody TerraformStateLock body) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Locking tfstate {}", tfstate);
        assert auth != null;
        log.info("Locking tfstate {}", body);

        try {
            return ResponseEntity.ok(terraformStateService.lock(tfstate, body, bucket, auth.getNamespace()));
        } catch (TerraformStateLockedError e) {
            return ResponseEntity.status(HttpStatus.LOCKED).body(e.getTerraformStateLock());
        } catch (TerraformStateConflictError e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getTerraformStateLock());
        }
    }

    @Operation(
            parameters = {
                @Parameter(name = "bucket", in = ParameterIn.PATH, description = "Name of the bucket", required = true),
                @Parameter(
                        name = "tfstate",
                        in = ParameterIn.PATH,
                        description = "Name of the tfstate file",
                        required = true)
            },
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/lock/{tfstate}")
    public void releaseLock(
            @PathVariable String bucket, @PathVariable String tfstate, @RequestBody Map<String, String> body) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Deleting tfstate lock {}", tfstate);
        assert auth != null;
        log.info("Deleting tfstate lock {}", body);
        terraformStateService.unlock(tfstate, bucket, auth.getNamespace());
    }
}
