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

import com.arpanrec.aphrodite.exceptions.BadClient;
import com.arpanrec.aphrodite.exceptions.GPGException;
import com.arpanrec.aphrodite.exceptions.KeyValueNotFoundException;
import com.arpanrec.aphrodite.models.Bucket;
import com.arpanrec.aphrodite.models.KeyValue;
import com.arpanrec.aphrodite.models.Namespace;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class KeyValueService {
    private final KeyValueRepository keyValueRepository;

    private final EncryptionService encryptionService;
    private final BucketsService bucketsService;
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public KeyValueService(
            @Autowired KeyValueRepository keyValueRepository,
            @Autowired EncryptionService encryptionService,
            @Autowired BucketsService bucketsService) {
        this.keyValueRepository = keyValueRepository;
        this.encryptionService = encryptionService;
        this.bucketsService = bucketsService;
    }

    public int getNextVersion(String key, Bucket bucket) {
        validateKey(key);
        log.trace("Getting top version for key: {}", key);
        Optional<Integer> currentKv = keyValueRepository.findTopVersion(key, bucket);
        int currentVal = currentKv.orElse(0);
        log.trace("Top version for key: {} is: {}", key, currentVal);
        return currentVal + 1;
    }

    public Map<String, Object> get(String key, int version, String bucket, Namespace namespace) {
        return get(key, version, bucketsService.get(bucket, namespace));
    }

    public Map<String, Object> get(String key, int version, Bucket bucket) {
        validateKey(key);
        log.trace("Getting value for key: {}", key);

        if (version == 0) {
            version = keyValueRepository
                    .findTopCurrentVersion(key, bucket)
                    .orElseThrow(() -> new KeyValueNotFoundException("Key not found: " + key));
        }

        log.trace("Reading version: {}, key: {}", version, key);
        KeyValue encryptedKV = keyValueRepository.findTopByKeyAndVersionAndBucketAndDeletedFalse(key, version, bucket);
        try {
            return OBJECT_MAPPER.readValue(
                    encryptionService.decrypt(encryptedKV.getEncryptedValue(), bucket.getNamespace()),
                    new TypeReference<>() {});
        } catch (Exception e) {
            throw new GPGException("Failed to decrypt value", e);
        }
    }

    public int save(String key, Map<String, Object> unencryptedValue, int version, String bucket, Namespace namespace) {
        return save(key, unencryptedValue, version, bucketsService.get(bucket, namespace));
    }

    public int save(String key, Map<String, Object> unencryptedValue, int version, Bucket bucket) {
        validateKey(key);
        KeyValue keyValue = new KeyValue();
        keyValue.setId(java.util.UUID.randomUUID().toString());
        keyValue.setKey(key);
        keyValue.setBucket(bucket);
        byte[] encryptedValue;
        try {
            encryptedValue = encryptionService.encrypt(
                    OBJECT_MAPPER.writeValueAsString(unencryptedValue), bucket.getNamespace());
        } catch (Exception e) {
            throw new GPGException("Failed to encrypt value", e);
        }
        keyValue.setEncryptedValue(encryptedValue);
        if (version == 0) {
            version = getNextVersion(key, bucket);
        }
        log.trace("Saving version: {}, key: {}, value: {}", version, key, encryptedValue);
        keyValue.setVersion(version);
        keyValue.setEncryptorHash(encryptionService.getEncryptorHash(bucket.getNamespace()));
        keyValue.setCreatedAt(Instant.now().toEpochMilli());
        return keyValueRepository.save(keyValue).getVersion();
    }

    public List<String> list(String key, String bucket, Namespace namespace) {
        return list(key, bucketsService.get(bucket, namespace));
    }

    public List<String> list(String key, Bucket bucket) {
        validateKeyForListing(key);
        if (key.isBlank()) {
            return keyValueRepository.findAllKeys(bucket);
        }
        return keyValueRepository.findAllKeysLike(key, bucket);
    }

    @Transactional
    public void delete(String key, int version, String bucket, Namespace namespace) {
        delete(key, version, bucketsService.get(bucket, namespace));
    }

    @Transactional
    void delete(String key, int version, Bucket bucket) {
        validateKey(key);
        log.trace("Deleting version: {}, key: {}", version, key);
        if (version != 0) {
            keyValueRepository.setDeletedTrue(key, version, bucket);
        } else {
            markAllVersionForDelete(key, bucket);
        }
    }

    @Transactional
    void markAllVersionForDelete(String key, Bucket bucket) {
        validateKey(key);
        log.trace("Deleting all versions of key: {}", key);
        keyValueRepository.setDeletedTrueAllVersions(key, bucket);
    }

    public KeyValueMetaData getMetaData(String key, String bucket, Namespace namespace) {
        return getMetaData(key, bucketsService.get(bucket, namespace));
    }

    public KeyValueMetaData getMetaData(String key, Bucket bucket) {
        validateKey(key);
        long lastCreatedAt = keyValueRepository
                .findLastCreatedVersion(key, bucket)
                .orElseThrow(() -> new BadClient("Key not found: " + key));
        long firstCreatedAt = keyValueRepository
                .findFirstCreatedVersion(key, bucket)
                .orElseThrow(() -> new BadClient("Key not found: " + key));

        List<Integer> versions = keyValueRepository.findAllVersions(key, bucket);
        return new KeyValueMetaData(lastCreatedAt, firstCreatedAt, versions);
    }

    public record KeyValueMetaData(Long lastCreatedAt, Long firstCreatedAt, List<Integer> versions) {}

    public static void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BadClient("Key cannot be null or blank");
        }

        validateKeyForListing(key);
    }

    public static void validateKeyForListing(String key) {
        if (key.startsWith("/") || key.endsWith("/")) {
            throw new BadClient("Key cannot start or end with a slash");
        }

        for (var code : key.toCharArray()) {
            boolean allowed = code == 47 // '/'
                    || (code >= 48 && code <= 57) // '0-9'
                    || (code >= 65 && code <= 90) // 'A-Z'
                    || (code >= 97 && code <= 122) // 'a-z'
                    || code == 95 // '_'
                    || code == 45; // '-'

            if (!allowed) {
                throw new BadClient(
                        "Invalid character '" + code + ", Char: " + code + "' in key. Allowed: /, 0-9, A-Z, a-z, _, -");
            }
        }
    }
}
