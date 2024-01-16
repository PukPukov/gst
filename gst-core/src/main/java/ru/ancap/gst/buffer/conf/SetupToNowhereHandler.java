package ru.ancap.gst.buffer.conf;

import ru.ancap.gst.buffer.PlaceholderHandler;

public interface SetupToNowhereHandler {
    
    void handle(String key, PlaceholderHandler handler);
    
}