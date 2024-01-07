package ru.ancap.gst.parser.simple.exception;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(fluent = true) @Getter
@ToString(callSuper = true)
public class RegularCharacterEscapedException extends InvalidEscapingException {

    private final int index;
    private final char escapedCharacter;

    public RegularCharacterEscapedException(String gst, int index, char escapedCharacter) {
        super(gst);
        this.index = index;
        this.escapedCharacter = escapedCharacter;
    }
    
    @Override
    public String getMessage() {
        return this.toString();
    }
    
}
