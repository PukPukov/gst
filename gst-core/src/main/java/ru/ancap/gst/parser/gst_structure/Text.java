package ru.ancap.gst.parser.gst_structure;

import lombok.With;

/**
 * @param index first text symbol 0-based index in original string
 */
@With
public record Text(int index, String string) implements GSTPart { }