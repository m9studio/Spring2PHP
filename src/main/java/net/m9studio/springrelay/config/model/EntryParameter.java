package net.m9studio.springrelay.config.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntryParameter {
    @NotNull
    @NotBlank
    String type;
    @NotNull
    @NotBlank
    String name;

    boolean required = true;
}
