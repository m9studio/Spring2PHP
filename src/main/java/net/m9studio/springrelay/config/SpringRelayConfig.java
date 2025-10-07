package net.m9studio.springrelay.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Component
@Validated
@ConfigurationProperties(prefix = "spring-relay")
public class SpringRelayConfig {
    @NotNull
    private String basePath;
    @NotNull
    private String baseTargetUrl;
    @NotNull
    private String configPath;

    private boolean ignoreInvalidEntries = false;
    private boolean failOnMultipleMatches = true;
}