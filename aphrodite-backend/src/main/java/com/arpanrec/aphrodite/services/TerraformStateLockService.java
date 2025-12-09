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

import com.arpanrec.aphrodite.models.TerraformStateLock;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TerraformStateLockService {
    TerraformStateLockRepository terraformStateLockRepository;

    public TerraformStateLockService(@Autowired TerraformStateLockRepository terraformStateLockRepository) {
        this.terraformStateLockRepository = terraformStateLockRepository;
    }

    public TerraformStateLock save(@NotNull TerraformStateLock terraformStateLock) {
        return terraformStateLockRepository.save(terraformStateLock);
    }
}
