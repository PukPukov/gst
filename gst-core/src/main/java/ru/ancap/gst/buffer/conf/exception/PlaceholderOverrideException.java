package ru.ancap.gst.buffer.conf.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.ancap.gst.buffer.PlaceholderHandler;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter
@ToString @EqualsAndHashCode(callSuper = false)
public class PlaceholderOverrideException extends RuntimeException {
    
    private final String key;
    private final PlaceholderHandler previousLogic;
    private final PlaceholderHandler newLogic;
    
    @Override
    public String getMessage() {
        return this.toString();
    }
    
}