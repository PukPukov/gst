package ru.ancap.gst.parser.simple.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter
@ToString
public abstract class IllegalGSTException extends RuntimeException {
    
    private final String gst;
    
}
