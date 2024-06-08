package ru.ancap.gst.parser;

public enum EscapingMode {

    /**
     * Placeholders will look like "\{id}", "{id}" is just text.
     * <p>
     * Longer, but support backward compatibility on adding new special constructions.
     * That way Java works with in-code strings (with exception to base quotes)
     */
    UNESCAPED_IS_SIMPLE_TEXT,
    
    /**
     * Placeholders will look like "{id}", "\{id}" is text "{id}".
     * <p>
     * Shorter, but old strings may break when adding new constructions
     * (for example, you had "red" keyword in some handler, and "blue" 
     * interpreted as simple text, then if you decided to add "blue" 
     * keyword all previous strings that used "blue" as text will change.
     */
    UNESCAPED_IS_PLACEHOLDER
    
}