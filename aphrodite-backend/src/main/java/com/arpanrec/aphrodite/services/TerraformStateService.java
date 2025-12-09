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

import com.arpanrec.aphrodite.exceptions.GPGException;
import com.arpanrec.aphrodite.exceptions.TerraformStateConflictError;
import com.arpanrec.aphrodite.exceptions.TerraformStateLockedError;
import com.arpanrec.aphrodite.models.Bucket;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.TerraformState;
import com.arpanrec.aphrodite.models.TerraformStateLock;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class TerraformStateService {
    private final TerraformStateRepository terraformStateRepository;

    private final EncryptionService encryptionService;
    private final BucketsService bucketsService;
    private final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final TerraformStateLockService terraformStateLockService;

    public TerraformStateService(
            @Autowired TerraformStateRepository terraformStateRepository,
            @Autowired EncryptionService encryptionService,
            @Autowired BucketsService bucketsService,
            @Autowired TerraformStateLockService terraformStateLockService) {
        this.terraformStateRepository = terraformStateRepository;
        this.encryptionService = encryptionService;
        this.bucketsService = bucketsService;
        this.terraformStateLockService = terraformStateLockService;
    }

    public int getNextVersion(String name, Bucket bucket) {
        log.trace("Getting top version for state: {}", name);
        Optional<Integer> currentKv = terraformStateRepository.findTopVersion(name, bucket);
        int currentVal = currentKv.orElse(0);
        log.trace("Top version for state: {} is: {}", name, currentVal);
        return currentVal + 1;
    }

    public @Nullable Map<String, Object> getState(String name, String bucket, Namespace namespace) {
        return getState(name, bucketsService.get(bucket, namespace));
    }

    public @Nullable Map<String, Object> getState(String name, Bucket bucket) {
        var encryptedState = this.get(name, bucket);
        if (encryptedState == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(
                    encryptionService.decrypt(encryptedState.getEncryptedState(), bucket.getNamespace()),
                    new TypeReference<>() {});
        } catch (Exception e) {
            throw new GPGException("Failed to decrypt terraform state", e);
        }
    }

    private TerraformState get(String name, Bucket bucket) {
        log.info("Fetching terraform state: {}", name);

        var version =
                terraformStateRepository.findTopCurrentVersion(name, bucket).orElse(null);

        if (version == null) {
            return null;
        }

        return terraformStateRepository.findTopByNameAndVersionAndBucketAndDeletedFalse(name, version, bucket);
    }

    public TerraformState save(
            String name, Map<String, Object> unencryptedState, String lockId, String bucket, Namespace namespace) {
        return save(name, unencryptedState, lockId, bucketsService.get(bucket, namespace));
    }

    public TerraformState save(String name, Map<String, Object> unencryptedValue, String lockId, Bucket bucket) {
        TerraformState currentState = get(name, bucket);
        TerraformState newTerraformState = new TerraformState();

        if (currentState != null && currentState.getLock() != null && (lockId == null || lockId.isBlank())) {
            throw new TerraformStateLockedError(currentState.getLock());
        }

        if (currentState != null
                && currentState.getLock() != null
                && !currentState.getLock().getID().equals(lockId)) {
            throw new TerraformStateConflictError(currentState.getLock());
        }

        var version = getNextVersion(name, bucket);

        newTerraformState.setId(java.util.UUID.randomUUID().toString());
        newTerraformState.setName(name);
        newTerraformState.setBucket(bucket);
        byte[] encryptedValue;
        try {
            encryptedValue = encryptionService.encrypt(
                    OBJECT_MAPPER.writeValueAsString(unencryptedValue), bucket.getNamespace());
        } catch (Exception e) {
            throw new GPGException("Failed to encrypt terraform state", e);
        }
        newTerraformState.setEncryptedState(encryptedValue);
        log.trace("Saving state: {}, key: {}, value: {}", version, name, encryptedValue);
        newTerraformState.setVersion(version);
        newTerraformState.setEncryptorHash(encryptionService.getEncryptorHash(bucket.getNamespace()));
        newTerraformState.setCreatedAt(Instant.now().toEpochMilli());
        return terraformStateRepository.save(newTerraformState);
    }

    public List<String> list(Bucket bucket) {
        return terraformStateRepository.findAllStates(bucket);
    }

    @Transactional
    public void markAllVersionForDelete(String name, String lockID, String bucket, Namespace namespace) {
        markAllVersionForDelete(name, lockID, bucketsService.get(bucket, namespace));
    }

    @Transactional
    public void markAllVersionForDelete(String name, String lockID, Bucket bucket) {
        log.trace("Deleting all versions of state: {}", name);
        var currentState = get(name, bucket);
        if (currentState != null
                && currentState.getLock() != null
                && !currentState.getLock().getID().equals(lockID)) {
            throw new TerraformStateConflictError(currentState.getLock());
        }

        terraformStateRepository.setDeletedTrueAllVersion(name, bucket);
    }

    @Transactional
    public TerraformStateLock lock(String name, TerraformStateLock lock, String bucket, Namespace namespace) {
        return lock(name, lock, bucketsService.get(bucket, namespace));
    }

    @Transactional
    public TerraformStateLock lock(String name, TerraformStateLock lock, Bucket bucket) {
        log.trace("Locking of state: {} with lockId: {}", name, lock);
        var currenTfs = get(name, bucket);
        if (currenTfs != null && currenTfs.getLock() != null) {
            throw new TerraformStateLockedError(currenTfs.getLock());
        }
        if (currenTfs == null) {
            currenTfs = save(name, Map.of(), null, bucket);
        }
        currenTfs.setLock(terraformStateLockService.save(lock));
        return terraformStateRepository.save(currenTfs).getLock();
    }

    @Transactional
    public void unlock(String name, String bucket, Namespace namespace) {
        unlock(name, bucketsService.get(bucket, namespace));
    }

    @Transactional
    public void unlock(String name, Bucket bucket) {
        log.info("Unlocking of state: {}", name);
        var currenTfs = get(name, bucket);
        assert currenTfs != null;
        currenTfs.setLock(null);
        terraformStateRepository.save(currenTfs);
    }
}
