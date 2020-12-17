package com.jacobsonmt.mags.ui.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class Message {
    public enum MessageLevel {
        INFO, WARNING, ERROR;
    }

    private MessageLevel level;
    private String message;
}
