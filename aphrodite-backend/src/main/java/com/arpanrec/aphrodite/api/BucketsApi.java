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
import com.arpanrec.aphrodite.models.Bucket;
import com.arpanrec.aphrodite.services.BucketsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/buckets")
@Tag(name = "Buckets", description = "Operations related to managing buckets.")
public class BucketsApi {

    private final BucketsService bucketsService;

    public BucketsApi(@Autowired BucketsService bucketsService) {
        this.bucketsService = bucketsService;
    }

    @Operation(
            summary = "List all buckets in the current namespace",
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public Iterable<@NotNull Bucket> listBuckets() {
        AuthenticationImpl auth = (AuthenticationImpl)
                Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        return bucketsService.list(auth.getNamespace());
    }

    public record BucketRequest(@NotNull String name) {}

    @Operation(
            summary = "Create a bucket in the current namespace",
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PostMapping(
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<@NotNull Bucket> createBucket(@RequestBody BucketRequest request) {
        @NotNull
        AuthenticationImpl auth = (AuthenticationImpl)
                Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Creating bucket: {}", request.name);
        Bucket bucket = bucketsService.create(request.name, auth.getNamespace());
        return new ResponseEntity<>(bucket, HttpStatus.CREATED);
    }

    public record BucketUpdateRequest(@NotNull String name) {}

    @Operation(
            summary = "Update a bucket in the current namespace",
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PutMapping(
            path = "/{bucketName}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<@NotNull Bucket> updateBucket(
            @PathVariable String bucketName, @RequestBody BucketUpdateRequest request) {
        @NotNull
        AuthenticationImpl auth = (AuthenticationImpl)
                Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Updating bucket: {}", bucketName);
        Bucket bucket = bucketsService.updateName(bucketName, request.name, auth.getNamespace());
        return new ResponseEntity<>(bucket, HttpStatus.OK);
    }

    @Operation(
            summary = "Delete an existing bucket name in the current namespace",
            security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @DeleteMapping(
            path = "/{bucketName}",
            consumes = {MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<@NotNull Void> deleteBucket(@PathVariable String bucketName) {
        AuthenticationImpl auth = (AuthenticationImpl)
                Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication());
        log.info("Deleting bucket: {}", bucketName);
        bucketsService.delete(bucketName, auth.getNamespace());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
