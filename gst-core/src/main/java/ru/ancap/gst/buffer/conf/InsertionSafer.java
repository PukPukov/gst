package ru.ancap.gst.buffer.conf;

public interface InsertionSafer {

    InsertionSafer UNSAFE = s -> s;

    String safe(String unsafe);
    
}
