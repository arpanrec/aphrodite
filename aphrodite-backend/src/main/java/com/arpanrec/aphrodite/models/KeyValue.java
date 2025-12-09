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
package com.arpanrec.aphrodite.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "key_value_t")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"key_c", "version_c", "bucket_id_c"})})
@AllArgsConstructor
@NoArgsConstructor
public class KeyValue {
    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "key_c", nullable = false)
    private String key;

    @Column(name = "encrypted_value_c", nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedValue;

    @Column(name = "deleted_c", nullable = false)
    private boolean deleted = false;

    @Column(name = "version_c", nullable = false)
    private Integer version;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @Column(name = "encryptor_hash_c", nullable = false)
    private String encryptorHash;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Bucket.class)
    @JoinColumn(name = "bucket_id_c", referencedColumnName = "id_c")
    private Bucket bucket;
}
