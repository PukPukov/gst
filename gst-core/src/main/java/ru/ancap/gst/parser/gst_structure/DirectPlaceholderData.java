package ru.ancap.gst.parser.gst_structure;

import java.util.Optional;

public record DirectPlaceholderData(String key, Optional<String> argument, Text originalPart) {
}
