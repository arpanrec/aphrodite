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

import com.arpanrec.aphrodite.ApplicationConstants;
import com.arpanrec.aphrodite.exceptions.AuthenticationError;
import com.arpanrec.aphrodite.models.ApiKey;
import com.arpanrec.aphrodite.models.Namespace;
import com.arpanrec.aphrodite.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final ApplicationPropertyServices applicationPropertyServices;
    private final Map<Namespace, SecretKey> jwtSecretKeys = new HashMap<>();

    public ApiKeyService(
            @Autowired ApiKeyRepository apiKeyRepository,
            @Autowired ApplicationPropertyServices applicationPropertyServices) {
        this.apiKeyRepository = apiKeyRepository;
        this.applicationPropertyServices = applicationPropertyServices;
    }

    public ApiKey findByIdAndNamespace(UUID kid, Namespace namespace) {
        Optional<ApiKey> apiKey = apiKeyRepository.findByIdAndNamespace(kid.toString(), namespace);
        return apiKey.orElse(null);
    }

    public SecretKey getJwtSecretKey(Namespace namespace) {
        if (jwtSecretKeys.containsKey(namespace)) {
            return jwtSecretKeys.get(namespace);
        }

        byte[] existingKey =
                applicationPropertyServices.get(ApplicationPropertyServices.PropertyKeys.JWT_SECRET_KEY, namespace);
        if (existingKey == null) {
            log.info("No JWT secret key found, generating new key");
            SecretKey newKey = Jwts.SIG.HS256.key().build();
            applicationPropertyServices.save(
                    ApplicationPropertyServices.PropertyKeys.JWT_SECRET_KEY, newKey.getEncoded(), namespace);
            jwtSecretKeys.put(namespace, newKey);
            return newKey;
        } else {
            SecretKey key = Keys.hmacShaKeyFor(existingKey);
            jwtSecretKeys.put(namespace, key);
            return key;
        }
    }

    public String generateToken(User user, String ip, String comment, long validitySeconds, Namespace namespace) {
        if (validitySeconds * 1000L > ApplicationConstants.MAX_API_KEY_MILI_SECONDS) {
            throw new AuthenticationError("API key validity cannot exceed "
                    + ApplicationConstants.MAX_API_KEY_MILI_SECONDS
                    + " milliseconds");
        }
        if (validitySeconds == 0) {
            validitySeconds = ApplicationConstants.MAX_API_KEY_MILI_SECONDS / 1000L;
        }
        long validityMiliSeconds = validitySeconds * 1000L;
        UUID kid = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiredAt = now.plusMillis(validityMiliSeconds);
        ApiKey apiKey = new ApiKey();
        apiKey.setId(kid.toString());
        apiKey.setCreatedAt(now.toEpochMilli());
        apiKey.setUser(user);
        apiKey.setComment(comment);
        apiKey.setOriginIp(ip);
        apiKey.setExpireAt(expiredAt.toEpochMilli());
        apiKey.setNamespace(namespace);
        apiKeyRepository.save(apiKey);

        return Jwts.builder()
                .header()
                .type("JWT")
                .keyId(kid.toString())
                .and()
                .issuedAt(Date.from(now))
                .signWith(this.getJwtSecretKey(namespace))
                .compact();
    }

    public ApiKey setLastUsedAt(ApiKey apiKey) {
        apiKey.setLastUsedAt(Instant.now().toEpochMilli());
        return apiKeyRepository.save(apiKey);
    }

    public ApiKey save(ApiKey apiKey) {
        return apiKeyRepository.save(apiKey);
    }

    public void delete(ApiKey apiKey) {
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }
}
