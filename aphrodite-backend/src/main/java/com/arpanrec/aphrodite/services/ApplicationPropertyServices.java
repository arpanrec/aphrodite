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

import com.arpanrec.aphrodite.models.ApplicationProperty;
import com.arpanrec.aphrodite.models.Namespace;
import java.time.Instant;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ApplicationPropertyServices {

    public enum PropertyKeys {
        JWT_SECRET_KEY
    }

    private final EncryptionService encryptionService;
    private final ApplicationPropertiesRepository applicationPropertiesRepository;

    public ApplicationPropertyServices(
            @Autowired EncryptionService encryptionService,
            @Autowired ApplicationPropertiesRepository applicationPropertiesRepository) {
        this.encryptionService = encryptionService;
        this.applicationPropertiesRepository = applicationPropertiesRepository;
    }

    public byte[] get(PropertyKeys property, Namespace namespace) {
        log.info("Getting property: {} in namespace: {}", property.name(), namespace.getName());
        Optional<ApplicationProperty> applicationProperty =
                applicationPropertiesRepository.findTopByKeyAndNamespaceOrderByVersionDesc(property.name(), namespace);
        return applicationProperty
                .map(value -> encryptionService.decrypt(value.getEncryptedValue(), namespace))
                .orElse(null);
    }

    public void save(PropertyKeys property, byte[] value, Namespace namespace) {
        log.info("Saving property: {} in namespace: {}", property.name(), namespace.getName());
        int existingPropertyVersion = 0;
        Optional<ApplicationProperty> existingProperty =
                applicationPropertiesRepository.findTopByKeyAndNamespaceOrderByVersionDesc(property.name(), namespace);
        if (existingProperty.isPresent()) {
            existingPropertyVersion = existingProperty.get().getVersion();
        }
        ApplicationProperty applicationProperty = new ApplicationProperty();
        applicationProperty.setKey(property.name());
        applicationProperty.setEncryptedValue(encryptionService.encrypt(value, namespace));
        applicationProperty.setVersion(existingPropertyVersion + 1);
        applicationProperty.setCreatedAt(Instant.now().toEpochMilli());
        String encryptorHash = encryptionService.getEncryptorHash(namespace);
        applicationProperty.setNamespace(namespace);
        applicationProperty.setEncryptorHash(encryptorHash);
        applicationPropertiesRepository.save(applicationProperty);
    }
}
