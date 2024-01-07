package ru.ancap.gst.buffer;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import ru.ancap.gst.buffer.conf.ConfExpectPlaceholderHandler;
import ru.ancap.gst.parser.gst_structure.DirectPlaceholderData;

@RequiredArgsConstructor
@ToString @EqualsAndHashCode
public class SimpleReplace implements ConfExpectPlaceholderHandler {
    
    private final String replacement;

    @Override
    public String handle(DirectPlaceholderData ignored) {
        return this.replacement;
    }
    
}
