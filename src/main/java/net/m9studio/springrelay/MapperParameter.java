package net.m9studio.springrelay;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapperParameter {
    private String name;
    private String type;
    private Boolean required = true;
}
