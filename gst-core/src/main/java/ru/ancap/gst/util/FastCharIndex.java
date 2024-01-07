package ru.ancap.gst.util;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Set;

/**
 * Faster than hashset on small amount of elements
 */
@RequiredArgsConstructor
@ToString @EqualsAndHashCode
public class FastCharIndex {
    
    private final char[] chars;
    
    public static FastCharIndex of(char... chars) {
        //noinspection ResultOfMethodCallIgnored
        Set.of(chars); // ensure there are no duplicates
        return new FastCharIndex(chars);
    }
    
    public boolean contains(char char_) {
        for (char compared : this.chars) if (compared == char_) return true;
        return false;
    }
    
}
