package ru.ancap.gst.buffer.conf;

import ru.ancap.gst.buffer.PlaceholderHandler;

public interface PlaceholderOverrideHandler {
    
    /**
     * @return allow to override?
     */
    boolean handle(String overridedKey, PlaceholderHandler prev, PlaceholderHandler next);
    
}