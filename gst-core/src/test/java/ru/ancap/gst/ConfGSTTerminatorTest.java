package ru.ancap.gst;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.ancap.gst.buffer.conf.ConfGSTTerminator;
import ru.ancap.gst.buffer.conf.OptionalHandler;
import ru.ancap.gst.buffer.conf.exception.PlaceholderOverrideException;
import ru.ancap.gst.buffer.conf.exception.PlaceholderProcessingException;
import ru.ancap.gst.buffer.conf.exception.PlaceholderSetupToNowhereException;
import ru.ancap.gst.buffer.conf.exception.UnhandledPlaceholderException;
import ru.ancap.gst.parser.gst_structure.Placeholder;
import ru.ancap.gst.parser.simple.SimpleGSTParser;
import ru.ancap.gst.parser.simple.exception.UnexpectedEndOfInputException;

import java.util.Set;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("DataFlowIssue") // it's test, all runtime error will become compile-time
public class ConfGSTTerminatorTest {
    
    @Test
    public void simple() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobuzzbaz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void findDeclaration() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("buzz", buffer.findDeclaration("bar").handle(Placeholder.DUMMY));
        assertNull(buffer.findDeclaration("baz"));
    }
    
    @Test
    public void argument() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", ph -> ph.argument().orElseThrow());
        
        assertEquals("foobuzzbaz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar:buzz}baz")));
    }
    
    @Test
    public void originalPart() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", ph -> ph.originalPart().index() + ph.originalPart().string());
        
        assertEquals("foo3\\{bar}baz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void declareALot() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        buffer.declare("bar2", "buzz2");
        buffer.declare("bar3", "buzz3");
        buffer.declare("bar4", "buzz4");
        
        assertEquals("foobuzzbazbuzz2buzz3buzz4", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz\\{bar2}\\{bar3}\\{bar4}")));
    }
    
    @Test
    public void useMultipleTimes() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobuzzbazbuzzbuzzbuzz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz\\{bar}\\{bar}\\{bar}")));
    }
    
    @Test
    public void unhandledPlaceholder() {
        assertThrows(UnhandledPlaceholderException.class, () -> ConfGSTTerminator.newStrict().build().terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz\\{bar2}\\{bar3}\\{bar4}")));
    }
    
    @Test
    public void unhandledPlaceholderLenient() {
        assertEquals("foo\\{bar}baz\\{bar2}\\{bar3}\\{bar4}", ConfGSTTerminator.newLenient().build().terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz\\{bar2}\\{bar3}\\{bar4}")));
    }
    
    @Test
    public void unhandledPlaceholderWarning() {
        var loggerMock = mock(Logger.class);
        assertEquals("foo\\{bar}", ConfGSTTerminator.newWarning(loggerMock).build().terminate(SimpleGSTParser.inst().parse("foo\\{bar}")));
        verify(loggerMock).throwing(any(String.class), any(String.class), any(UnhandledPlaceholderException.class));
    }
    
    @Test
    public void placeholderSetupToNowhere() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertThrows(PlaceholderSetupToNowhereException.class, () -> buffer.terminate(SimpleGSTParser.inst().parse("foobaz")));
    }
    
    @Test
    public void placeholderSetupToNowhereLenient() {
        var buffer = ConfGSTTerminator.newLenient().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobaz", buffer.terminate(SimpleGSTParser.inst().parse("foobaz")));
    }
    
    @Test
    public void placeholderSetupToNowhereWarning() {
        var loggerMock = mock(Logger.class);
        var buffer = ConfGSTTerminator.newWarning(loggerMock).build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobaz", buffer.terminate(SimpleGSTParser.inst().parse("foobaz")));
        verify(loggerMock).throwing(any(String.class), any(String.class), any(PlaceholderSetupToNowhereException.class));
    }
    
    @Test
    public void placeholderSetupToExclusion() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobaz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{!bar}baz")));
    }
    
    @Test
    public void externalExcludedKeys() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("foobaz", buffer.terminate(SimpleGSTParser.inst().parse("foobaz"), Set.of("bar")));
    }
    
    @Test
    public void placeholderOverride() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        assertThrows(PlaceholderOverrideException.class, () -> buffer.declare("bar", "buzz2"));
    }
    
    @Test
    public void placeholderOverrideLenient() {
        var buffer = ConfGSTTerminator.newLenient().build();
        buffer.declare("bar", "buzz");
        buffer.declare("bar", "buzz2");
        
        assertEquals("foobuzz2baz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void placeholderOverrideWarning() {
        var loggerMock = Mockito.mock(Logger.class);
        var buffer = ConfGSTTerminator.newWarning(loggerMock).build();
        buffer.declare("bar", "buzz");
        buffer.declare("bar", "buzz2");
        
        verify(loggerMock).throwing(any(String.class), any(String.class), any(PlaceholderOverrideException.class));
        assertEquals("foobuzz2baz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void placeholderProcessingException() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", ph -> ph.argument().orElseThrow()); // should throw since no argument
        
        assertThrows(PlaceholderProcessingException.class, () -> buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void placeholderProcessingExceptionLenient() {
        var buffer = ConfGSTTerminator.newLenient().build();
        buffer.declare("bar", ph -> ph.argument().orElseThrow()); // should throw since no argument
        
        assertEquals("foo\\{bar}baz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void placeholderProcessingExceptionWarning() {
        var loggerMock = Mockito.mock(Logger.class);
        var buffer = ConfGSTTerminator.newWarning(loggerMock).build();
        buffer.declare("bar", ph -> ph.argument().orElseThrow()); // should throw since no argument
        
        assertEquals("foo\\{bar}baz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
        verify(loggerMock).throwing(any(String.class), any(String.class), any(PlaceholderProcessingException.class));
    }
    
    @Test
    public void unexpectedEndOfInput() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertThrows(UnexpectedEndOfInputException.class, () -> buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar:arg")));
    }
    
    @Test
    public void unexpectedEndOfInputLenient() {
        var buffer = ConfGSTTerminator.newLenient().build();
        buffer.declare("bar", (ph) -> {
            assertEquals("arg", ph.argument().orElseThrow()); // don't need to assert called since comparing returned result at next line with expected
            return "buzz";
        });
        
        assertEquals("foobuzz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar:arg")));
    }
    
    @Test
    public void unexpectedEndOfInputWarning() {
        var loggerMock = mock(Logger.class);
        var buffer = ConfGSTTerminator.newWarning(loggerMock).build();
        buffer.declare("bar", (ph) -> {
            assertEquals("arg", ph.argument().orElseThrow()); // don't need to assert called since comparing returned result at next line with expected
            return "buzz";
        });
        
        assertEquals("foobuzz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar:arg")));
        verify(loggerMock).throwing(any(String.class), any(String.class), any(UnexpectedEndOfInputException.class));
    }
    
    @Test
    public void externalExclusion() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        buffer.declare("bar2", "buzz2");
        buffer.declare("bar3", "buzz3");
        buffer.declare("bar4", "buzz4");

        assertEquals("foobuzzbaz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz"), Set.of("bar2", "bar3", "bar4")));
    }
    
    @Test
    public void dontAcceptOverridesButNotThrow() {
        var buffer = ConfGSTTerminator.newLenient()
            .placeholderOverrideHandler(OptionalHandler.checking((ign1, ign2, ign3) -> false)).build();
        buffer.declare("bar", "buzz");
        buffer.declare("bar", "buzz2");
        
        assertEquals("foobuzzbaz", buffer.terminate(SimpleGSTParser.inst().parse("foo\\{bar}baz")));
    }
    
    @Test
    public void empty() {
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("bar", "buzz");
        
        assertEquals("", buffer.terminate(SimpleGSTParser.inst().parse(""), Set.of("bar")));
    }
    
}