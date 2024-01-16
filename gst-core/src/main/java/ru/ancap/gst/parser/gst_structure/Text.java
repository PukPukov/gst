package ru.ancap.gst.parser.gst_structure;

/**
 * @param index first text symbol 0-based index in original string
 */
public record Text(int index, String string) implements GSTPart { }