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

import com.arpanrec.aphrodite.ApplicationConstants;
import com.arpanrec.aphrodite.exceptions.NameSpaceNotFoundException;
import com.arpanrec.aphrodite.models.Namespace;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NamespaceService {

    private final NamespaceRepository nameSpaceRepository;

    public NamespaceService(@Autowired NamespaceRepository nameSpaceRepository) {
        this.nameSpaceRepository = nameSpaceRepository;
    }

    public Iterable<Namespace> getAll() {
        return nameSpaceRepository.findAll();
    }

    public Optional<Namespace> getOptional(String name) {
        if (name == null || name.isBlank()) {
            name = ApplicationConstants.NAMESPACE_DEFAULT;
        }
        return nameSpaceRepository.findByName(name);
    }

    public Namespace get(String name) {
        if (name == null || name.isBlank()) {
            name = ApplicationConstants.NAMESPACE_DEFAULT;
        }
        String finalNamespace = name;
        return getOptional(finalNamespace)
                .orElseThrow(() -> new NameSpaceNotFoundException("Namespace not found: " + finalNamespace));
    }

    public Namespace getOrCreate(String name) {
        if (name == null || name.isBlank()) {
            name = ApplicationConstants.NAMESPACE_DEFAULT;
        }
        String finalNamespace = name;
        return nameSpaceRepository
                .findByName(name)
                .orElseGet(() -> nameSpaceRepository.save(new Namespace(
                        UUID.randomUUID().toString(),
                        finalNamespace,
                        Instant.now().toEpochMilli())));
    }
}
