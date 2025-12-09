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

import com.arpanrec.aphrodite.models.Bucket;
import com.arpanrec.aphrodite.models.KeyValue;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyValueRepository extends JpaRepository<@NotNull KeyValue, @NotNull String> {

    @Query("select max(kv.version) from key_value_t kv where kv.key = ?1 and kv.deleted = false and kv.bucket = ?2")
    Optional<Integer> findTopCurrentVersion(String key, Bucket bucket);

    @Query("select max(kv.version) from key_value_t kv where kv.key = ?1 and kv.bucket = ?2")
    Optional<Integer> findTopVersion(String key, Bucket bucket);

    @Query("select kv.version from key_value_t kv where kv.key = ?1 and kv.bucket = ?2 and kv.deleted = false")
    List<Integer> findAllVersions(String key, Bucket bucket);

    KeyValue findTopByKeyAndVersionAndBucketAndDeletedFalse(String key, Integer version, Bucket bucket);

    @Modifying
    @Query("update key_value_t kv set kv.deleted = true where kv.key = ?1 and kv.bucket = ?2")
    void setDeletedTrueAllVersion(String key, Bucket bucket);

    @Modifying
    @Query("update key_value_t kv set kv.deleted = true where kv.key = ?1 and kv.version = ?2 and kv.bucket = ?3")
    void setDeletedTrue(String key, int version, Bucket bucket);

    @Query("select distinct kv.key from key_value_t kv where kv.key like ?1% and kv.deleted = false and kv.bucket = ?2")
    List<String> findAllKeysLike(String key, Bucket bucket);

    @Query("select distinct kv.key from key_value_t kv where kv.deleted = false and kv.bucket = ?1")
    List<String> findAllKeys(Bucket bucket);

    @Query("select min(kv.createdAt) from key_value_t kv where kv.key = ?1 and kv.bucket = ?2")
    Optional<Long> findFirstCreatedVersion(String key, Bucket bucket);

    @Query("select max(kv.createdAt) from key_value_t kv where kv.key = ?1 and kv.bucket = ?2")
    Optional<Long> findLastCreatedVersion(String key, Bucket bucket);
}
