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
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "buckets_t")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"namespace_id_c", "name_c"})})
@AllArgsConstructor
@NoArgsConstructor
public class Bucket implements Serializable {

    @Serial
    private static final long serialVersionUID = -8207626016676925694L;

    @Id
    @Column(name = "id_c")
    private String id;

    @Column(name = "name_c", nullable = false)
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;

    @Column(name = "created_at_c", nullable = false)
    private long createdAt;
}
