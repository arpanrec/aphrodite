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
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "namespace_t")
@Table(
        uniqueConstraints = {@UniqueConstraint(columnNames = {"name_c"})} // Doesn't work with SQLite
        )
@AllArgsConstructor
@NoArgsConstructor
public class Namespace implements Serializable {

    @Serial
    private static final long serialVersionUID = 7392954992759510152L;

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "name_c", nullable = false, unique = true)
    private String name;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Namespace other) {
            return this.name.equals(other.name) && this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }
}
