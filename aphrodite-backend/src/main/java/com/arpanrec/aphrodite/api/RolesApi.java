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
import com.arpanrec.aphrodite.models.Role;
import com.arpanrec.aphrodite.services.RoleServices;
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
@RequestMapping(path = ApplicationConstants.API_ENDPOINT + "/roles")
@Tag(name = "Role Management", description = "Operations related to managing roles in the system.")
public class RolesApi {
    private final RoleServices rolesServices;

    public RolesApi(@Autowired RoleServices rolesServices) {
        this.rolesServices = rolesServices;
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(path = "/{rolesName}")
    @ResponseStatus(value = HttpStatus.OK)
    public Role getRole(@PathVariable String rolesName) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        log.info("Reading roles: {}", rolesName);
        assert auth != null;
        return rolesServices.getRole(rolesName, auth.getNamespace());
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @GetMapping(path = "/")
    @ResponseStatus(value = HttpStatus.OK)
    public Iterable<String> listRoles() {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return rolesServices.list(auth.getNamespace());
    }

    public record CreateRoleRequest(String name, String description, String[] privileges) {}

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PostMapping(path = "/")
    @ResponseStatus(value = HttpStatus.CREATED)
    public Role createRole(@RequestBody CreateRoleRequest createRoleRequest) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return rolesServices.createRole(
                createRoleRequest.name,
                createRoleRequest.description,
                createRoleRequest.privileges,
                auth.getNamespace());
    }

    public record UpdateRoleRequest(String description, String[] privileges) {}

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @PutMapping(path = "/{rolesName}")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    public Role updateRole(@PathVariable String rolesName, @RequestBody UpdateRoleRequest updateRoleRequest) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        return rolesServices.updateRole(
                rolesName, updateRoleRequest.description, updateRoleRequest.privileges, auth.getNamespace());
    }

    @Operation(security = {@SecurityRequirement(name = ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME)})
    @DeleteMapping(path = "/{rolesName}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable String rolesName) {
        AuthenticationImpl auth =
                (AuthenticationImpl) SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        rolesServices.delete(rolesName, auth.getNamespace());
    }
}
