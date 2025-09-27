package net.m9studio.spring2php;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring2php")
public class Config {
    private String baseRouter;
    private String phpBaseUrl;
    private String configPath;
}