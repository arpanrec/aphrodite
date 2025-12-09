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
package com.arpanrec.aphrodite.api;

import com.arpanrec.aphrodite.ApplicationConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.log4j.Log4j2;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
@OpenAPIDefinition
public class SpringDocsConfig {
    private final String appName;

    public SpringDocsConfig(@Value("${spring.application.name:unknown}") String appName) {
        this.appName = appName;
    }

    @Bean
    public OpenAPI customize() {
        var openApi = new OpenAPI();
        var version = getClass().getPackage().getImplementationVersion();
        if (version == null || version.isBlank()) {
            version = "unknown";
        }

        log.info("Configuring SpringDocs for API key authentication");
        var securityItem = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(ApplicationConstants.API_KEY_HEADER);
        var securityComponents =
                new Components().addSecuritySchemes(ApplicationConstants.OPENAPI_SECURITY_SCHEME_NAME, securityItem);
        openApi.components(securityComponents);

        openApi.info(new io.swagger.v3.oas.models.info.Info()
                .title(appName)
                .version(version)
                .description("API Documentation"));

        return openApi;
    }

    @Bean
    public OpenApiCustomizer customApiWithNamespaceHeader() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (!path.toLowerCase().startsWith(ApplicationConstants.API_ENDPOINT.toLowerCase())) {
                return;
            }
            if (path.toLowerCase()
                    .startsWith(ApplicationConstants.API_ENDPOINT + "/" + ApplicationConstants.INIT_ENDPOINT)) {
                return;
            }
            log.debug("Adding namespace header to path: {}", path);
            pathItem.addParametersItem(new Parameter()
                    .name(ApplicationConstants.NAMESPACE_HEADER)
                    .in(ParameterIn.HEADER.name())
                    .required(false)
                    .schema(new StringSchema()._default(ApplicationConstants.NAMESPACE_DEFAULT))
                    .allowEmptyValue(true)
                    .description("Namespace header for API requests"));
            pathItem.getParameters().forEach(parameter -> {
                var paramIn = parameter.getIn();
                parameter.setIn(paramIn.toLowerCase());
            });
        });
    }
}
