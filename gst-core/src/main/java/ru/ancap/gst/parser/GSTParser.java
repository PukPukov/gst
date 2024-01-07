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
    
    /*default PARSED prepare(String string) {
        return this.handle(string, new GSTReplacementBuffer<>(), TerminationSettings.DEFAULT());
    }
    default PARSED handle(String generalStringTemplate, GSTReplacementBuffer<PARSED> buffer) {
        return this.handle(generalStringTemplate, buffer, AbstractBaseGSTParser.TerminationSettings.DEFAULT());
    }
    default PARSED handle(String generalStringTemplate, GSTReplacementBuffer<PARSED> buffer, TerminationSettings<PARSED> settings) {
        return this.handle(new GeneralStringTemplate(generalStringTemplate, List.of()), buffer, settings);
    }
    default PARSED handle(GeneralStringTemplate template, GSTReplacementBuffer<PARSED> buffer) {
        return this.handle(template, buffer, AbstractBaseGSTParser.TerminationSettings.DEFAULT());
    }

    //PARSED handle(GeneralStringTemplate template, GSTReplacementBuffer<PARSED> buffer, TerminationSettings<PARSED> settings);

    record TerminationSettings<STRING>(Function<STRING, STRING> placeholderSafer, TerminationErrorHandler<STRING> errorHandler) {
        public static <STRING> TerminationSettings<STRING> DEFAULT() { return new TerminationSettings<>(s -> s, TerminationErrorHandler.FAIL_FAST()); }
    }*/


}
