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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "encryption_keys_t")
@AllArgsConstructor
@NoArgsConstructor
public class EncryptionKey {

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "hash_c", nullable = false)
    private String keyHash;

    @Column(name = "encryptor_hash_c", nullable = false)
    private String encryptorHash;

    @Column(name = "encrypted_key_c", nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedKey;

    @Column(name = "encrypted_password_c", nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedPassword;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;
}
