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

import com.arpanrec.aphrodite.exceptions.AuthenticationError;
import com.arpanrec.aphrodite.hash.Argon2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
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
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "users_t")
@Table(
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"username_c", "namespace_id_c"}),
            @UniqueConstraint(columnNames = {"email_c", "namespace_id_c"})
        })
public class User implements UserDetails, CredentialsContainer {

    @Id
    @Column(name = "id_c")
    @Getter
    @Setter
    private String id;

    @JsonIgnore
    private static final long EXPIRATION = 7L * 24L * 60L * 60L * 1_000_000_000L;

    @Serial
    private static final long serialVersionUID = -8372582194659560207L;

    @JsonIgnore
    @Setter
    @Column(name = "username_c", nullable = false, unique = true)
    private String username;

    @Getter
    @Setter
    @Column(name = "email_c")
    private String email;

    @JsonIgnore
    @Column(name = "password_hash_c")
    private String passwordHash;

    @JsonIgnore
    @Column(name = "password_last_changed_c")
    private long passwordLastChanged;

    @Setter
    @Getter
    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class)
    @JoinTable(
            name = "users_to_roles_t",
            joinColumns = @JoinColumn(name = "user_id_c", referencedColumnName = "id_c"),
            inverseJoinColumns = @JoinColumn(name = "role_id_c", referencedColumnName = "id_c"))
    private Set<Role> roles = new HashSet<>();

    @Setter
    @Column(name = "expired_c")
    private boolean expired;

    @Setter
    @Column(name = "locked_c")
    private boolean locked;

    @Setter
    @Column(name = "enabled_c")
    private boolean enabled;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = Namespace.class)
    @JoinColumn(name = "namespace_id_c", referencedColumnName = "id_c")
    private Namespace namespace;

    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
        this.passwordLastChanged = Instant.now().toEpochMilli();
    }

    public void setPassword(String password) {
        this.passwordHash = Argon2.INSTANCE.encode(password);
        this.passwordLastChanged = Instant.now().toEpochMilli();
    }

    @JsonIgnore
    @Override
    public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public @NotNull String getUsername() {
        return this.username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return Instant.now().toEpochMilli() - passwordLastChanged < EXPIRATION;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return !expired;
    }

    @Override
    public String toString() {
        return "username: " + username + ", email: " + email + ", passwordHash: " + passwordHash
                + ", passwordLastChanged: " + passwordLastChanged + ", roles: " + roles
                + ", expired: " + expired + ", locked" + ": " + locked + ", enabled: " + enabled;
    }

    public void isValidLoginAttemptOrError(String password) {
        if (!Argon2.INSTANCE.matches(password, this.passwordHash)) {
            throw new AuthenticationError("Invalid password");
        }
        isValidUserOrError();
    }

    private void isValidUserOrError() {
        if (!this.isEnabled()) {
            throw new AuthenticationError("User account is disabled");
        }
        if (!this.isAccountNonExpired()) {
            throw new AuthenticationError("User account has expired");
        }
        if (!this.isAccountNonLocked()) {
            throw new AuthenticationError("User account is locked");
        }
        if (!this.isCredentialsNonExpired()) {
            throw new AuthenticationError("User credentials have expired");
        }
    }
}
