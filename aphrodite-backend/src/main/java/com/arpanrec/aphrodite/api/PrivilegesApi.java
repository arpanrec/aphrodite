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
import com.arpanrec.aphrodite.models.Privilege;
import com.arpanrec.aphrodite.services.PrivilegeServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/privileges")
@Tag(name = "Privilege Management", description = "Operations related to managing privileges in the system.")
public class PrivilegesApi {
    private final PrivilegeServices privilegeServices;

    public PrivilegesApi(@Autowired PrivilegeServices privilegeServices) {
        this.privilegeServices = privilegeServices;
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(path = "/{privilegeName}")
    @ResponseStatus(value = HttpStatus.OK)
    public Privilege getPrivilege(@PathVariable String privilegeName) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Reading privilege: {}", privilegeName);
        return privilegeServices.getPrivilege(privilegeName, auth.getNamespace());
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(path = "/")
    @ResponseStatus(value = HttpStatus.OK)
    public Iterable<String> listPrivileges() {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        return privilegeServices.list(auth.getNamespace());
    }

    public record CreatePrivilegeRequest(String name, String description) {}

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PostMapping(path = "/")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Privilege createPrivilege(@RequestBody CreatePrivilegeRequest createPrivilegeRequest) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        return privilegeServices.createPrivilege(
                createPrivilegeRequest.name, createPrivilegeRequest.description, auth.getNamespace());
    }

    public record UpdatePrivilegeRequest(String description) {}

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PutMapping(path = "/{privilegeName}")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Privilege updatePrivilege(
            @PathVariable String privilegeName, @RequestBody UpdatePrivilegeRequest updatePrivilegeRequest) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        return privilegeServices.updatePrivilege(
                privilegeName, updatePrivilegeRequest.description, auth.getNamespace());
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @DeleteMapping(path = "/{privilegeName}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deletePrivilege(@PathVariable String privilegeName) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        privilegeServices.delete(privilegeName, auth.getNamespace());
    }
}
