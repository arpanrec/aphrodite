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
package com.arpanrec.aphrodite.services;

import com.arpanrec.aphrodite.ApplicationConfigurations;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.Privilege;
import com.arpanrec.aphrodite.models.Role;
import com.arpanrec.aphrodite.models.User;
import com.arpanrec.aphrodite.utils.FileUtils;
import com.arpanrec.aphrodite.utils.PasswordGenerator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RootPasswordService {
    private final UserService userService;
    private final ApplicationConfigurations configurations;
    private final NamespaceService namespaceService;
    private final PrivilegeServices privilegeServices;
    private final RoleServices roleServices;

    public RootPasswordService(
            @Autowired UserService userService,
            @Autowired ApplicationConfigurations configurations,
            @Autowired NamespaceService namespaceService,
            @Autowired PrivilegeServices privilegeServices,
            @Autowired RoleServices roleServices) {
        this.userService = userService;
        this.configurations = configurations;
        this.namespaceService = namespaceService;
        this.privilegeServices = privilegeServices;
        this.roleServices = roleServices;
    }

    public void createRootUsers() {
        Iterable<Namespace> listOfNamespaces = namespaceService.getAll();
        listOfNamespaces.forEach(this::createRootUser);
    }

    public void createRootUser(Namespace namespace) {
        String rootPasswordFromConfig = configurations.getRootPasswords().get(namespace.getName());
        Optional<User> rootUserMaybe = userService.findByUsernameAndNamespace("root", namespace);
        User rootUser = rootUserMaybe.orElseGet(() -> {
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            return user;
        });
        Privilege rootPrivilege = privilegeServices.getOrUpdatePrivilege("root", "Root privilege", namespace);
        Set<Privilege> rootPrivileges = new HashSet<>();
        rootPrivileges.add(rootPrivilege);
        Role rootRole = roleServices.getOrUpdateRole("root", "Root role", rootPrivileges, namespace);
        rootRole.setRolePrivUri("(?s).*");
        rootRole.setRolePrivUriAllowedMethods(Set.of(
                // https://www.rfc-editor.org/rfc/rfc9110.html#name-method-definitions
                "GET", "HEAD", "POST", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE"));
        Set<Role> rootRoles = new HashSet<>();
        rootRoles.add(rootRole);
        rootUser.getRoles().clear();
        rootUser.setRoles(rootRoles);
        rootUser.setUsername("root");
        rootUser.setEnabled(true);
        rootUser.setExpired(false);
        if (rootPasswordFromConfig == null && rootUser.getPassword() == null) {
            String randomPassword = PasswordGenerator.INSTANCE.generate(256, true, true, true, true);
            log.info("Generating random password for root user: {}", randomPassword);
            rootUser.setPassword(randomPassword);
        }
        if (rootPasswordFromConfig != null) {
            log.info("Resetting root user password from config on namespace: {}", namespace.getName());
            rootUser.setPassword(FileUtils.INSTANCE.fileOrString(rootPasswordFromConfig));
        } else {
            log.info("Not resetting root user password on namespace: {}", namespace.getName());
        }
        userService.saveUser(rootUser, namespace);
        log.info("Root user settings complete, root user: {}", rootUser);
    }
}
