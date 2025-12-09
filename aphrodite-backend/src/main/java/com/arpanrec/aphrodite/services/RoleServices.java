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

import com.arpanrec.aphrodite.exceptions.AuthenticationError;
import com.arpanrec.aphrodite.exceptions.RoleFoundException;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.Privilege;
import com.arpanrec.aphrodite.models.Role;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServices {

    private final RoleRepository roleRepository;
    private final PrivilegeServices privilegeServices;

    public RoleServices(@Autowired RoleRepository roleRepository, @Autowired PrivilegeServices privilegeServices) {
        this.roleRepository = roleRepository;
        this.privilegeServices = privilegeServices;
    }

    public Role getRole(String name, Namespace namespace) {
        return roleRepository
                .findByNameAndNamespace(name, namespace)
                .orElseThrow(() -> new RoleFoundException(
                        "Role not found for name: " + name + " in namespace: " + namespace.getName()));
    }

    public Role getOrUpdateRole(String name, String Description, String[] privileges, Namespace namespace) {
        Set<Privilege> privilegesSet = new HashSet<>();
        for (String privilege : privileges) {
            privilegesSet.add(privilegeServices.getPrivilege(privilege, namespace));
        }
        return getOrUpdateRole(name, Description, privilegesSet, namespace);
    }

    public Role getOrUpdateRole(String name, String Description, Set<Privilege> privileges, Namespace namespace) {
        Role role = roleRepository.findByNameAndNamespace(name, namespace).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setId(UUID.randomUUID().toString());
            newRole.setName(name);
            newRole.setCreatedAt(System.currentTimeMillis());
            return newRole;
        });
        role.setDescription(Description);
        role.getPrivileges().clear();
        for (Privilege privilege : privileges) {
            role.getPrivileges().add(privilege);
        }
        role.setNamespace(namespace);
        return roleRepository.save(role);
    }

    public Role createRole(String name, String Description, String[] privileges, Namespace namespace) {
        Set<Privilege> privilegesSet = new HashSet<>();
        for (String privilege : privileges) {
            privilegesSet.add(privilegeServices.getPrivilege(privilege, namespace));
        }
        return createRole(name, Description, privilegesSet, namespace);
    }

    public Role createRole(String name, String Description, Set<Privilege> privileges, Namespace namespace) {
        if (name == null || name.isBlank()) {
            return null;
        }
        Role role = new Role();
        role.setId(UUID.randomUUID().toString());
        role.setDescription(Description);
        role.setName(name);
        role.setCreatedAt(System.currentTimeMillis());
        role.setNamespace(namespace);
        role.getPrivileges().clear();
        for (Privilege privilege : privileges) {
            role.getPrivileges().add(privilege);
        }
        return roleRepository.save(role);
    }

    public Role updateRole(String name, String description, String[] privileges, Namespace namespace) {
        Set<Privilege> privilegesSet = new HashSet<>();
        for (String privilege : privileges) {
            privilegesSet.add(privilegeServices.getPrivilege(privilege, namespace));
        }
        return updateRole(name, description, privilegesSet, namespace);
    }

    public Role updateRole(String name, String description, Set<Privilege> privileges, Namespace namespace) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (name.equalsIgnoreCase("root")) {
            throw new AuthenticationError("Root role cannot be updated");
        }
        Role role = getRole(name, namespace);
        role.setDescription(description);
        role.getPrivileges().clear();
        for (Privilege privilege : privileges) {
            role.getPrivileges().add(privilege);
        }
        return roleRepository.save(role);
    }

    public Iterable<String> list(Namespace namespace) {
        return roleRepository.findAllByNamespace(namespace);
    }

    public void delete(String name, Namespace namespace) {
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.equalsIgnoreCase("root")) {
            throw new AuthenticationError("Root role cannot be deleted");
        }
        roleRepository.deleteByNameAndNamespace(name, namespace);
    }
}
