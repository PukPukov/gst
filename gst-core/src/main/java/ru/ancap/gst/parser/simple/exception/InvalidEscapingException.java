package ru.ancap.gst.parser.simple.exception;

public abstract class InvalidEscapingException extends IllegalGSTException {
    
    public InvalidEscapingException(String gst) {
        super(gst);
    }
    
}