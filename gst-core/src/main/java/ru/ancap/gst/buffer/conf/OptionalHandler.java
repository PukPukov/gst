package ru.ancap.gst.buffer.conf;

public interface OptionalHandler<T> {
    
    boolean checkForProblem();
    T handler();
    
    static <T> OptionalHandler<T> checking(T handler) {
        return new OptionalHandler<>() {
            @Override
            public boolean checkForProblem() {
                return true;
            }
            
            @Override
            public T handler() {
                return handler;
            }
        };
    }
    
}