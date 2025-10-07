package net.m9studio.springrelay.config.model;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Validated
public class Entry {
    //if(path == null && fullPath == null) - exception
    String path;
    String basePath;
    String fullPath;

    //if(targetUrl == null && fullTargetUrl == null) - exception
    String targetUrl;
    String baseTargetUrl;
    String fullTargetUrl;

    @NotNull
    String httpMethod;

    List<EntryParameter> parameters;


    @AssertTrue(message = "Specify either 'fullPath' OR 'path' (optionally with 'basePath').")
    public boolean isPathValid() {
        boolean hasFull = fullPath != null && !fullPath.isBlank();
        boolean hasRel  = path != null && !path.isBlank();
        return (hasFull ^ hasRel);
    }

    // Валидация "либо fullTargetUrl, либо (baseTargetUrl + targetUrl)"
    @AssertTrue(message = "Specify either 'fullTargetUrl' OR 'targetUrl' (optionally with 'baseTargetUrl').")
    public boolean isTargetValid() {
        boolean hasFull = fullTargetUrl != null && !fullTargetUrl.isBlank();
        boolean hasRel  = targetUrl != null && !targetUrl.isBlank();
        return (hasFull ^ hasRel);
    }
}
