package ru.ancap.gst.buffer.conf;

public class LenientOptionalHandler<T> implements OptionalHandler<T> {
    
    @Override
    public boolean checkForProblem() {
        return false;
    }

    @Override
    public T handler() {
        throw new IllegalStateException(
            "OptionalHandler#handle() should never be called in does ignore problem mode. " +
            "Most likely it is a bug in GST."
        );
    }
    
}
