package ru.ancap.gst.buffer.conf;

/**
 * Original name and special case of InsertionHandler
 */
public interface InsertionSafer extends InsertionHandler {
    
    InsertionSafer UNSAFE = s -> s;
    
    String safe(String unsafe);
    
    default String handle(String insertion) {
        return this.safe(insertion);
    }
    
}