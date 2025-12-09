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

@Entity(name = "api_keys_t")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiKey {

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @Column(name = "last_used_at_c", nullable = false)
    private long lastUsedAt;

    @Column(name = "expire_at_c", nullable = false)
    private long expireAt;

    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = User.class)
    @JoinColumn(name = "user_id_c", referencedColumnName = "id_c")
    private User user;

    @Column(name = "comment_c", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "origin_ip_c", columnDefinition = "VARCHAR(15)")
    private String originIp;

    @Column(name = "is_active_c", columnDefinition = "BOOLEAN")
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;
}
