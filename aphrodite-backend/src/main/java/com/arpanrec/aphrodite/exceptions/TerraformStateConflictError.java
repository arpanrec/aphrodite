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
package com.arpanrec.aphrodite.exceptions;

import com.arpanrec.aphrodite.models.TerraformStateLock;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Terraform State Conflict")
public class TerraformStateConflictError extends AuthenticationException {

    @Getter
    @Setter
    @NotNull
    private TerraformStateLock terraformStateLock;

    @Serial
    private static final long serialVersionUID = 1646335643763L;

    public TerraformStateConflictError(@NotNull TerraformStateLock terraformStateLock) {
        super("Terraform State Conflict");
        this.terraformStateLock = terraformStateLock;
    }
}
