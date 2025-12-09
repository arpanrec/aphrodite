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
import com.arpanrec.aphrodite.models.TerraformState;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TerraformStateRepository extends JpaRepository<@NotNull TerraformState, @NotNull String> {

    @Query("select max(tfstate.version) from terraform_state_t tfstate "
            + "where tfstate.name = ?1 and tfstate.deleted = false and tfstate.bucket = ?2")
    Optional<Integer> findTopCurrentVersion(String name, Bucket bucket);

    @Query("select max(kv.version) from terraform_state_t kv where kv.name = ?1 and kv.bucket = ?2")
    Optional<Integer> findTopVersion(String name, Bucket bucket);

    TerraformState findTopByNameAndVersionAndBucketAndDeletedFalse(String name, Integer version, Bucket bucket);

    @Modifying
    @Query("update terraform_state_t tfstate set tfstate.deleted = true"
            + " where tfstate.name = ?1 and tfstate.bucket = ?2")
    void setDeletedTrueAllVersion(String name, Bucket bucket);

    @Query("select distinct tfstate.name from terraform_state_t tfstate"
            + " where tfstate.deleted = false and tfstate.bucket = ?1")
    List<String> findAllStates(Bucket bucket);
}
