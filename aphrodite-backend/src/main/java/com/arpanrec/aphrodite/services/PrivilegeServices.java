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
import com.arpanrec.aphrodite.exceptions.PrivilegeFoundException;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.Privilege;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrivilegeServices {

    private final PrivilegeRepository privilegeRepository;

    public PrivilegeServices(@Autowired PrivilegeRepository privilegeRepository) {
        this.privilegeRepository = privilegeRepository;
    }

    public Privilege getPrivilege(String privilegeName, Namespace namespace) {
        return privilegeRepository
                .findByNameAndNamespace(privilegeName, namespace)
                .orElseThrow(() -> new PrivilegeFoundException(
                        "Privilege not found for name: " + privilegeName + " in namespace: " + namespace.getName()));
    }

    public Privilege createPrivilege(String name, String description, Namespace namespace) {
        Privilege privilege = new Privilege();
        privilege.setId(UUID.randomUUID().toString());
        privilege.setDescription(description);
        privilege.setName(name);
        privilege.setCreatedAt(System.currentTimeMillis());
        privilege.setNamespace(namespace);
        return privilegeRepository.save(privilege);
    }

    public Privilege getOrUpdatePrivilege(String name, String description, Namespace namespace) {
        Privilege privilege = privilegeRepository
                .findByNameAndNamespace(name, namespace)
                .orElseGet(() -> {
                    Privilege newPrivilege = new Privilege();
                    newPrivilege.setId(UUID.randomUUID().toString());
                    newPrivilege.setName(name);
                    newPrivilege.setCreatedAt(System.currentTimeMillis());
                    newPrivilege.setNamespace(namespace);
                    return newPrivilege;
                });
        if (description != null && !description.isBlank()) {
            privilege.setDescription(description);
        }
        return privilegeRepository.save(privilege);
    }

    public Privilege updatePrivilege(String name, String description, Namespace namespace) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (name.equalsIgnoreCase("root")) {
            throw new AuthenticationError("Root privilege cannot be updated");
        }
        Privilege privilege = getPrivilege(name, namespace);
        privilege.setDescription(description);
        return privilegeRepository.save(privilege);
    }

    public Iterable<String> list(Namespace namespace) {
        return privilegeRepository.findAllByNamespace(namespace);
    }

    public void delete(String name, Namespace namespace) {
        if (name == null || name.isBlank()) {
            return;
        }
        if (name.equalsIgnoreCase("root")) {
            throw new AuthenticationError("Root privilege cannot be deleted");
        }
        privilegeRepository.deleteByNameAndNamespace(name, namespace);
    }
}
