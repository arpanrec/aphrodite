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

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity(name = "terraform_state_lock_t")
@Table
@AllArgsConstructor
@NoArgsConstructor
public class TerraformStateLock {

    @Id
    @JsonProperty("ID")
    @Column(name = "id_c")
    String ID;

    @JsonProperty("Operation")
    @Column(name = "operation_c", nullable = false)
    String Operation;

    @Column(name = "info_c", nullable = false)
    @JsonProperty("Info")
    String Info;

    @Column(name = "who_c", nullable = false)
    @JsonProperty("Who")
    String Who;

    @Column(name = "version_c", nullable = false)
    @JsonProperty("Version")
    String Version;

    @Column(name = "created_c", nullable = false)
    @JsonProperty("Created")
    String Created;

    @Column(name = "path_c", nullable = false)
    @JsonProperty("Path")
    String Path;
}
