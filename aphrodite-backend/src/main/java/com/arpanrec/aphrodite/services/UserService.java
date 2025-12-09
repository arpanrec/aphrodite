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

import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.User;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
// public class UserService implements UserDetailsService {
// Global AuthenticationManager configured with an AuthenticationProvider bean. UserDetailsService beans will not be
// used by Spring Security for automatically configuring username/password login. Consider removing the
// AuthenticationProvider bean. Alternatively, consider using the UserDetailsService in a manually instantiated
// DaoAuthenticationProvider. If the current configuration is intentional, to turn off this warning, increase the
// logging level of 'org.springframework.security.config.annotation.authentication.configuration
// .InitializeUserDetailsBeanManagerConfigurer' to ERROR
public class UserService {

    private final UserRepository userRepository;

    public UserService(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUsernameAndNamespace(String username, Namespace namespace) {
        return userRepository.findByUsernameAndNamespace(username, namespace);
    }

    public User saveUser(User user, Namespace namespace) {
        user.setNamespace(namespace);
        return userRepository.save(user);
    }
}
