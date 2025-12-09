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
package com.arpanrec.aphrodite;

import com.arpanrec.aphrodite.exceptions.GPGException;
import com.arpanrec.aphrodite.services.EncryptionService;
import com.arpanrec.aphrodite.services.RootPasswordService;
import com.arpanrec.aphrodite.utils.FileUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class Bootstrap implements CommandLineRunner {

    private final EncryptionService encryptionService;
    private final ApplicationConfigurations configurations;
    private final RootPasswordService rootPasswordService;

    private final Environment environment;

    public Bootstrap(
            @Autowired EncryptionService encryptionService,
            @Autowired ApplicationConfigurations configurations,
            @Autowired RootPasswordService rootPasswordService,
            @Autowired Environment environment) {
        this.encryptionService = encryptionService;
        this.configurations = configurations;
        this.rootPasswordService = rootPasswordService;
        this.environment = environment;
    }

    private void loadUnlockKeys() {
        if (configurations.getUnlockKeys().isEmpty()) {
            log.info("No unlock key provided in config, skipping unlock");
            return;
        }
        configurations.getUnlockKeys().forEach(unlockKeyFromConfig -> {
            try {
                loadUnlockKeys(unlockKeyFromConfig);
            } catch (Exception e) {
                log.warn("Failed to load unlock key from config", e);
            }
        });
    }

    private void loadUnlockKeys(String unlockKeyFromConfig) {
        unlockKeyFromConfig = FileUtils.INSTANCE.fileOrString(unlockKeyFromConfig);
        try {
            unlockKeyFromConfig = new String(Base64.getDecoder().decode(unlockKeyFromConfig), StandardCharsets.UTF_8);
            log.info("Unlock key provided in config is base64 encoded.");
        } catch (RuntimeException e) {
            log.warn("Unlock key provided in config is not base64 encoded, ignoring");
        }

        unlockKeyFromConfig = FileUtils.INSTANCE.fileOrString(unlockKeyFromConfig);

        try {
            var unlockKey = new ObjectMapper().readValue(unlockKeyFromConfig, EncryptionService.UnlockKey.class);
            log.info("Loading master encryption key from config");
            encryptionService.loadMasterEncryptionKey(unlockKey);
        } catch (RuntimeException | JsonProcessingException e) {
            throw new GPGException("Failed to load unlock key from config", e);
        }
    }

    @Override
    public void run(String @NotNull ... args) throws RuntimeException {
        log.info("Post start up");
        boolean prodProfilePresent = isProdProfilePresent();

        if (!prodProfilePresent) {
            var errorMsg = "`production` profile is not active, this is not recommended for production";
            log.warn(errorMsg);
            System.err.println(errorMsg);
        }

        Security.addProvider(new BouncyCastleProvider());
        log.info("Bouncy castle provider added");
        loadUnlockKeys();
        rootPasswordService.createRootUsers();
        log.info("Application initialized");
    }

    private boolean isProdProfilePresent() {
        boolean defaultProfilePresent = false;
        boolean prodProfilePresent = false;
        for (String profile : environment.getActiveProfiles()) {
            if (profile.equals("default")) {
                defaultProfilePresent = true;
            } else if (profile.equals("production")) {
                prodProfilePresent = true;
            }
        }

        if (!defaultProfilePresent && prodProfilePresent) {
            var errorMsg = "`production` profile is active without `default` profile.\n"
                    + "Please activate `production` profile instead with `default` profile,"
                    + " -Dspring.profiles.active=default,production";
            log.error(errorMsg);
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        return prodProfilePresent;
    }
}
