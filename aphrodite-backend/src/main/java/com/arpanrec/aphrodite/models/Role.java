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

import com.arpanrec.aphrodite.attributeconverters.SetString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "roles_t")
@Getter
@Setter
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"name_c", "namespace_id_c"}),
        })
public class Role implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = 989235129409752804L;

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "name_c", nullable = false)
    private String name;

    @Column(name = "description_c")
    private String description;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @Column(name = "role_priv_uri_c", nullable = true)
    private String rolePrivUri;

    @Column(name = "role_priv_uri_allowed_methods_c", nullable = true)
    @Convert(converter = SetString.class)
    private Set<String> rolePrivUriAllowedMethods = new HashSet<>();

    @Column(name = "role_priv_uri_denied_methods_c", nullable = true)
    @Convert(converter = SetString.class)
    private Set<String> rolePrivUriDeniedMethods = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Privilege.class)
    @JoinTable(
            name = "roles_to_privileges_t",
            joinColumns = @JoinColumn(name = "role_id_c", referencedColumnName = "id_c"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id_c", referencedColumnName = "id_c"))
    private Set<Privilege> privileges = new HashSet<>();

    @Getter
    @Setter
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;

    @JsonIgnore
    @Override
    public String getAuthority() {
        return "ROLE_" + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Role other) {
            return this.name.equals(other.name) && this.namespace.equals(other.namespace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, namespace);
    }
}
