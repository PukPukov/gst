package ru.ancap.gst;

import lombok.With;

import java.util.Collection;

@With
public record GeneralStringTemplate(String base, Collection<String> excludedKeys) { }