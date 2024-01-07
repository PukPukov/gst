package ru.ancap.gst.buffer;

import ru.ancap.gst.parser.gst_structure.Placeholder;

public interface PlaceholderHandler {
    
    String handle(Placeholder placeholder);
    
}
