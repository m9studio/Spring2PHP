package net.m9studio.springrelay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ConfigurationProperties(prefix = "SpringRelay")
public class SpringRelayConfig {
    @NotNull
    private String basePath;
    @NotNull
    private String baseTargetUrl;
    @NotNull
    private String configPath;
}