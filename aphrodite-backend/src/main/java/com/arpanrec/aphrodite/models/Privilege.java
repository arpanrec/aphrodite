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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "privileges_t")
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"name_c", "namespace_id_c"}),
        })
public class Privilege implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = -597136067542369700L;

    @Id
    @Column(name = "id_c")
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @Column(name = "name_c", nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(name = "description_c")
    private String description;

    @Getter
    @Setter
    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @Getter
    @Setter
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;

    @Override
    @JsonIgnore
    public String getAuthority() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Privilege other) {
            return this.name.equals(other.name) && this.namespace.equals(other.namespace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace);
    }
}
