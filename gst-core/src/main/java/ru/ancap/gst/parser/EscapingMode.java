package ru.ancap.gst.parser;

public enum EscapingMode {

    UNESCAPED_IS_SIMPLE_TEXT, // placeholders will look like "\{id}", "{id}" is just text
    UNESCAPED_IS_PLACEHOLDER  // placeholders will look like "{id}", "\{id}" is text "{id}"

}