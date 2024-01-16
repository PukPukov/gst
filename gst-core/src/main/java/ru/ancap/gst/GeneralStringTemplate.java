package ru.ancap.gst;

import java.util.Collection;

public record GeneralStringTemplate(String base, Collection<String> excludedKeys) { }