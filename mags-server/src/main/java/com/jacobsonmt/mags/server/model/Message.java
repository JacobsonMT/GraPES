package com.jacobsonmt.mags.server.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Message {
    public enum MessageLevel {
        INFO, WARNING, ERROR;
    }

    private final MessageLevel level;
    private final String message;
}
