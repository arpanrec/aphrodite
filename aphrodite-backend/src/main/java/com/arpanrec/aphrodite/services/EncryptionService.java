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
import com.arpanrec.aphrodite.encryption.GnuPG;
import com.arpanrec.aphrodite.exceptions.GPGException;
import com.arpanrec.aphrodite.exceptions.NotInitializedException;
import com.arpanrec.aphrodite.hash.Sha512;
import com.arpanrec.aphrodite.models.EncryptionKey;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.utils.PasswordGenerator;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class EncryptionService {

    private final Map<Namespace, Pair<@NotNull String, @NotNull GnuPG>> encryptor = new HashMap<>();

    private final EncryptionKeyRepository encryptionKeyRepository;
    private final NamespaceService nameSpaceService;
    private final RootPasswordService rootPasswordService;

    public EncryptionService(
            @Autowired EncryptionKeyRepository encryptionKeyRepository,
            @Autowired NamespaceService nameSpaceService,
            @Autowired RootPasswordService rootPasswordService) {
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.nameSpaceService = nameSpaceService;
        this.rootPasswordService = rootPasswordService;
    }

    public String getEncryptorHash(Namespace namespace) {
        return encryptor.get(namespace).getFirst();
    }

    public void loadMasterEncryptionKey(UnlockKey unlockKey) {
        if (this.encryptor.containsKey(unlockKey.nameSpace)) {
            throw new GPGException("Application already unlocked");
        }
        if (unlockKey.b64EncryptionKey == null || unlockKey.b64EncryptionKey.isBlank()) {
            throw new GPGException("Invalid unlock key");
        }

        byte[] unlockKeyEncryptionKey = Base64.getDecoder().decode(unlockKey.b64EncryptionKey);

        String unlockKeyHash = Sha512.INSTANCE.encode(unlockKeyEncryptionKey);

        EncryptionKey masterKey = encryptionKeyRepository
                .findByIdAndEncryptorHashAndKeyHashAndNamespace(
                        unlockKey.id, unlockKeyHash, unlockKey.encryptedKeyHash, unlockKey.nameSpace)
                .orElseThrow(() -> new NotInitializedException("Invalid unlock key"));

        GnuPG unsealEncryptor = new GnuPG(unlockKeyEncryptionKey, unlockKey.encryptionKeyPassword);
        byte[] decryptedMasterKey = unsealEncryptor.decrypt(masterKey.getEncryptedKey());
        String decryptedMasterKeyPassword =
                new String(unsealEncryptor.decrypt(masterKey.getEncryptedPassword()), StandardCharsets.UTF_8);

        String masterKeyHash = Sha512.INSTANCE.encode(decryptedMasterKey);

        log.info("Loading master key");
        this.encryptor.put(
                unlockKey.nameSpace, Pair.of(masterKeyHash, new GnuPG(decryptedMasterKey, decryptedMasterKeyPassword)));
        log.info("Loaded master key");
        rootPasswordService.createRootUser(unlockKey.nameSpace);
    }

    public UnlockKey initializeApplication(InitializeDetails initializeDetails) {
        Namespace nameSpace = nameSpaceService.getOrCreate(initializeDetails.namespace);

        Optional<EncryptionKey> optionalMasterKey =
                encryptionKeyRepository.findTopByNamespaceIdOrderByIdDesc(nameSpace.getId());
        if (optionalMasterKey.isPresent()) {
            throw new GPGException("Application Already Initialized");
        }

        log.info("Initializing master encryption key");
        String masterGpgKeyPassword = PasswordGenerator.INSTANCE.generate(256, true, true, true, true);
        byte[] masterGpgKey = GnuPG.Companion.createGpgPrivateKey(
                initializeDetails.appName, initializeDetails.appEmail, masterGpgKeyPassword, 0);
        log.info("Generated master encryption key");

        log.info("Initializing unseal encryption key");
        String unsealGpgKeyPassword = PasswordGenerator.INSTANCE.generate(256, true, true, true, true);
        byte[] unsealGpgKey = GnuPG.Companion.createGpgPrivateKey(
                initializeDetails.appName, initializeDetails.appEmail, unsealGpgKeyPassword, 0);
        log.info("Generated unseal encryption key");

        GnuPG unsealEncryptor = new GnuPG(unsealGpgKey, unsealGpgKeyPassword);

        byte[] encryptedMasterGpgKey = unsealEncryptor.encrypt(masterGpgKey, null);
        byte[] encryptedMasterKeyPassword = unsealEncryptor.encrypt(masterGpgKeyPassword.getBytes(), null);

        String masterKeyHash = Sha512.INSTANCE.encode(masterGpgKey);

        EncryptionKey masterEncryptionKey = new EncryptionKey();
        masterEncryptionKey.setId(UUID.randomUUID().toString());
        masterEncryptionKey.setEncryptedKey(encryptedMasterGpgKey);
        masterEncryptionKey.setEncryptedPassword(encryptedMasterKeyPassword);
        masterEncryptionKey.setCreatedAt(Instant.now().toEpochMilli());
        masterEncryptionKey.setKeyHash(masterKeyHash);
        masterEncryptionKey.setEncryptorHash(Sha512.INSTANCE.encode(unsealGpgKey));
        masterEncryptionKey.setNamespace(nameSpace);
        EncryptionKey masterKeyRefetch = encryptionKeyRepository.save(masterEncryptionKey);

        rootPasswordService.createRootUser(nameSpace);

        return new UnlockKey(
                Base64.getEncoder().encodeToString(unsealGpgKey),
                unsealGpgKeyPassword,
                masterKeyHash,
                masterKeyRefetch.getId(),
                nameSpace);
    }

    public byte[] encrypt(String data, Namespace namespace) {
        if (!encryptor.containsKey(namespace)) {
            throw new NotInitializedException("Application not initialized, cannot encrypt string data");
        }
        return encrypt(data.getBytes(), namespace);
    }

    public byte[] encrypt(byte[] data, Namespace namespace) {
        if (!encryptor.containsKey(namespace)) {
            throw new NotInitializedException("Application not initialized, cannot encrypt string data");
        }
        return encryptor.get(namespace).getSecond().encrypt(data, null);
    }

    public byte[] decrypt(byte[] data, Namespace namespace) {
        if (!encryptor.containsKey(namespace)) {
            throw new NotInitializedException("Application not initialized, cannot decrypt data");
        }
        return encryptor.get(namespace).getSecond().decrypt(data);
    }

    public record UnlockKey(
            String b64EncryptionKey,
            String encryptionKeyPassword,
            String encryptedKeyHash,
            String id,
            Namespace nameSpace) {}

    @Data
    public static class InitializeDetails {
        String appName;
        String appEmail;

        String namespace = ApplicationConstants.NAMESPACE_DEFAULT;
    }
}
