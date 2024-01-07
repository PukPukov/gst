package ru.ancap.gst.buffer.conf.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.ancap.gst.parser.gst_structure.Placeholder;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter
@ToString
public class UnhandledPlaceholderException extends RuntimeException {

    private final Placeholder unhandledPlaceholder;
    
    @Override
    public String getMessage() {
        return this.toString();
    }

}