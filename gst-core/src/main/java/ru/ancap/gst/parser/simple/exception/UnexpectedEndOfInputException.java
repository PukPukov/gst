package ru.ancap.gst.parser.simple.exception;

import lombok.RequiredArgsConstructor;
import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.util.LinkedObjects;

@RequiredArgsConstructor
public class UnexpectedEndOfInputException extends RuntimeException {
    
    private final LinkedObjects<GSTPart> gst;
    
}
