package ru.ancap.gst.parser;

import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.util.LinkedObjects;

/**
 * General String Template parser. Build on the idea of direct submitting values to direct places, 
 * defined in template. 
 * <p>
 * Should support these features:<br>
 * — Simple keys (syntax may be like {ph1})<br>
 * — Placeholders with argument (syntax may be like {ph2:arg})<br>
 * — In-line placeholder exclusion (syntax may be like {!ph3})<br>
 * <p>
 * Syntax and additional features depend on implementation.<br>
 */
public interface GSTParser {
    
    LinkedObjects<GSTPart> parse(String string);
    
}