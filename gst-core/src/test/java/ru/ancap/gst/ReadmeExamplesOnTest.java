package ru.ancap.gst;

import org.apache.commons.math3.primes.Primes;
import org.junit.jupiter.api.Test;
import ru.ancap.gst.buffer.conf.ConfGSTTerminator;
import ru.ancap.gst.parser.EscapingMode;
import ru.ancap.gst.parser.GSTParser;
import ru.ancap.gst.parser.SpecialCharacterSet;
import ru.ancap.gst.parser.simple.SimpleGSTParser;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadmeExamplesOnTest {
    
    @Test
    public void exampleTestSimple() {
        GSTParser parser = SimpleGSTParser.inst();
        
        var buffer = ConfGSTTerminator.newStrict().build();
        buffer.declare("foo", "fizz");
        buffer.declare("bar", "buzz");
        
        String result = buffer.terminate(parser.parse("foo = \\{foo}; bar = \\{bar}")); // output is "foo = fizz; bar = buzz"
        
        assertEquals("foo = fizz; bar = buzz", result);
    }
    
    @Test
    public void exampleTestFineTuned() {
        GSTParser parser = SimpleGSTParser.builder()
            .specialCharacterSet(SpecialCharacterSet.builder()
                .closure('(', ')')
                .argumentDelimiter('_').build())
            .escapingMode(EscapingMode.UNESCAPED_IS_PLACEHOLDER).build();
        
        var buffer = ConfGSTTerminator.newLenient().build(); // fail-safe
        
        buffer.declare("isPrime", (placeholder) -> ""+ Primes.isPrime(Integer.parseInt(placeholder.argument().orElseThrow())));
        
        String result1 = buffer.terminate(parser.parse("(isPrime_14); (isPrime_7)"   ));   // output is "false; true"
        String result2 = buffer.terminate(parser.parse("\\(isPrime_14); (isPrime_7)" ));   // output is "(isPrime_14); true"
        String result3 = buffer.terminate(parser.parse("(isPrime_foo); (isPrime_7)"  ));   // output is "(isPrime_foo); true"
        String result4 = buffer.terminate(parser.parse("(isPrime_foo); (isPrime_7"   ));   // output is "(isPrime_foo); true"
        
        assertEquals("false; true", result1);
        assertEquals("(isPrime_14); true", result2);
        assertEquals("(isPrime_foo); true", result3);
        assertEquals("(isPrime_foo); true", result4);
    }
    
}