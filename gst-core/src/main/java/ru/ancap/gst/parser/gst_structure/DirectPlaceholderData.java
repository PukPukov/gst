package ru.ancap.gst.parser.gst_structure;

import lombok.With;

import java.util.Optional;

@With
public record DirectPlaceholderData(String key, Optional<String> argument, Text originalPart) { }