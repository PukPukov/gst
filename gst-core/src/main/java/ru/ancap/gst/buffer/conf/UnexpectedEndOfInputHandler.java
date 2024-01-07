package ru.ancap.gst.buffer.conf;

import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.util.LinkedObjects;

public interface UnexpectedEndOfInputHandler {
    
    void handle(LinkedObjects<GSTPart> templateWithUnexpectedEnd);
    
}
