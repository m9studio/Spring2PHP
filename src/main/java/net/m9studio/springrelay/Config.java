package net.m9studio.springrelay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "SpringRelay")
public class Config {
    private String baseRouter;
    private String phpBaseUrl;
    private String configPath;
}