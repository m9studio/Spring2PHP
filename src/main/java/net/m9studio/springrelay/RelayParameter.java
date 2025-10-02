package net.m9studio.springrelay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelayParameter {
    private String name;
    private String type;
    private Boolean required = true;
}
