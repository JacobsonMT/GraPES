package com.jacobsonmt.mags.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Taxa {
    public enum KnownKeyTypes {
        OX,
        missing_OX,
        malformed_OX,
        Invalid_OX,
        Virus_OX,
        Invalid_OX_format
    }

    private final String key;
    private final int id;
    private final String name;
}
