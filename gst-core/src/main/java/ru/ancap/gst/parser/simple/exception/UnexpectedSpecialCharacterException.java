package ru.ancap.gst.parser.simple.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.ancap.gst.parser.simple.exception.IllegalGSTException;

@Accessors(fluent = true) @Getter
@EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class UnexpectedSpecialCharacterException extends IllegalGSTException {
    
    private final int index;
    private final char illegalSpecialCharacter;
    
    public UnexpectedSpecialCharacterException(String gst, int index, char character) {
        super(gst);
        this.index = index;
        this.illegalSpecialCharacter = character;
    }

    @Override
    public String getMessage() {
        return this.toString();
    }
    
}
