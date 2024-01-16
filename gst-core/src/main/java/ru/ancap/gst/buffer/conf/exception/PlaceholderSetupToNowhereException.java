package ru.ancap.gst.buffer.conf.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.ancap.gst.buffer.PlaceholderHandler;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter
@ToString
public class PlaceholderSetupToNowhereException extends RuntimeException {
    
    private final String key;
    private final PlaceholderHandler handler;
    
    @Override
    public String getMessage() {
        return this.toString();
    }
    
}