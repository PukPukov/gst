package ru.ancap.gst.parser.gst_structure;

import java.util.Optional;

public record Placeholder(DirectPlaceholderData directData, boolean exclusion, boolean endExpected) implements GSTPart {
    
    public static final Placeholder DUMMY = new Placeholder(new DirectPlaceholderData("foo", Optional.empty(), new Text(0, "\\{foo}")), false, true);
    
}
