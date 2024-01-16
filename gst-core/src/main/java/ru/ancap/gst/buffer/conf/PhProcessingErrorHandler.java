package ru.ancap.gst.buffer.conf;

import ru.ancap.gst.parser.gst_structure.DirectPlaceholderData;

public interface PhProcessingErrorHandler {
    
    String handle(DirectPlaceholderData placeholder, Throwable thrown);
    
}