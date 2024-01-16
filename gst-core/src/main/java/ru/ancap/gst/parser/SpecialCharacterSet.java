package ru.ancap.gst.parser;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString @EqualsAndHashCode
@Accessors(fluent = true) @Getter
@lombok.Builder(builderClassName = "Builder")
public final class SpecialCharacterSet {
    
    public static final SpecialCharacterSet DEFAULT = SpecialCharacterSet.builder().build();
    
    @lombok.Builder.Default private final char opening = '{';
    @lombok.Builder.Default private final char closing = '}';
    @lombok.Builder.Default private final char argumentDelimiter = ':';
    @lombok.Builder.Default private final char exclusionChar = '!';
    @lombok.Builder.Default private final char escapingCharacter = '\\';
    
    public static class Builder {
        
        public Builder closure(char opening, char closing) {
            this.opening(opening);
            this.closing(closing);
            return this;
        }
        
    }
    
}