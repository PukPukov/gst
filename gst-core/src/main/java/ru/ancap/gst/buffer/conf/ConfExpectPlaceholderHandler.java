package ru.ancap.gst.buffer.conf;

import ru.ancap.gst.parser.gst_structure.DirectPlaceholderData;

public interface ConfExpectPlaceholderHandler {
    
    String handle(DirectPlaceholderData directPhData);
    
}
