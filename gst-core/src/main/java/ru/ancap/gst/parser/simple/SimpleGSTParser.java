package ru.ancap.gst.parser.simple;

import lombok.*;
import lombok.experimental.Accessors;
import ru.ancap.commons.parse.EscapingBuffer;
import ru.ancap.gst.parser.EscapingMode;
import ru.ancap.gst.parser.GSTParser;
import ru.ancap.gst.parser.SpecialCharacterSet;
import ru.ancap.gst.parser.gst_structure.DirectPlaceholderData;
import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.parser.gst_structure.Placeholder;
import ru.ancap.gst.parser.gst_structure.Text;
import ru.ancap.gst.parser.simple.exception.RegularCharacterEscapedException;
import ru.ancap.gst.parser.simple.exception.UnexpectedSpecialCharacterException;
import ru.ancap.gst.util.FastCharIndex;
import ru.ancap.gst.util.LinkedObjects;

import java.util.Optional;

/**
 * Simple implementation of GST parser with support of most basic features such as escaping and charset configuration.<br>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
@ToString @EqualsAndHashCode
public class SimpleGSTParser implements GSTParser {
    
    private final SpecialCharacterSet specialCharacterSet;
    private final EscapingMode escapingMode;
    private final FastCharIndex specialCharactersIndex;
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Accessors(fluent = true, chain = true) @Setter
    public static class Builder {
        
        private SpecialCharacterSet specialCharacterSet = SpecialCharacterSet.DEFAULT;
        private EscapingMode escapingMode = EscapingMode.UNESCAPED_IS_SIMPLE_TEXT;
        
        public Builder specialCharacterSet(SpecialCharacterSet specialCharacterSet) {
            this.specialCharacterSet = specialCharacterSet;
            return this;
        }
        
        public SimpleGSTParser build() {
            return new SimpleGSTParser(
                this.specialCharacterSet,
                this.escapingMode,
                FastCharIndex.of(this.specialCharacterSet.opening(), this.specialCharacterSet.closing(), this.specialCharacterSet.argumentDelimiter(), this.specialCharacterSet.exclusionChar(), this.specialCharacterSet.escapingCharacter())
            );
        }
        
    }
    
    public static SimpleGSTParser inst() {
        return SimpleGSTParser.builder().build();
    }
    
    @Override
    public LinkedObjects<GSTPart> parse(String template) {
        char[] string = template.toCharArray();
        
        EscapingBuffer escapingBuffer = new EscapingBuffer();
        
        ParseState state = new ParseState();
        
        int partStartIndex = 0;
        
        for (int index = 0; index < string.length; index++) {
            escapingBuffer.step();
            char char_ = string[index];
            if (char_ == this.specialCharacterSet.escapingCharacter() && (!escapingBuffer.currentlyEscaped())) {
                escapingBuffer.escapeNext();
                if (state.filling == FillState.FILLING_ID || state.filling == FillState.FILLING_ARGUMENT) state.specialPartBuilder.append(char_);
                continue;
            }
            boolean isSpecial = this.specialCharactersIndex.contains(char_);
            if (escapingBuffer.currentlyEscaped() && !isSpecial) throw new RegularCharacterEscapedException(template, index, char_);
            
            switch (state.filling) {
                case FILLING_TEXT -> {
                    boolean shouldEscapePh = this.escapingMode == EscapingMode.UNESCAPED_IS_SIMPLE_TEXT;
                    boolean canBePhByEscapingRule = shouldEscapePh == escapingBuffer.currentlyEscaped();
                    if (this.specialCharacterSet.opening() == char_ && canBePhByEscapingRule) {
                        state.filling = FillState.FILLING_ID;
                        if (!state.textBuffer.isEmpty()) state.parsedGST.add(new Text(partStartIndex, state.textBuffer.toString()));
                        partStartIndex = index - (this.escapingMode == EscapingMode.UNESCAPED_IS_SIMPLE_TEXT ? 1 : 0);
                        state.specialPartBuilder = new StringBuilder(escapingBuffer.currentlyEscaped() ? "\\"+char_ : ""+char_);
                        state.placeholderIdBuilder = new StringBuilder(16);
                        continue;
                    }
                    state.textBuffer.append(char_);
                }
                case FILLING_ID -> {
                    if (!escapingBuffer.currentlyEscaped()) {
                        if (char_ == this.specialCharacterSet.exclusionChar()) {
                            state.specialPartBuilder.append(char_);
                            state.fillingExclusion = true;
                            continue;
                        }
                        if (char_ == this.specialCharacterSet.argumentDelimiter()) {
                            state.filling = FillState.FILLING_ARGUMENT;
                            state.specialPartBuilder.append(char_);
                            state.argumentBuilder = new StringBuilder(20);
                            state.hasArgument = true;
                            continue;
                        }
                        if (char_ == this.specialCharacterSet.closing()) {
                            state.specialPartBuilder.append(char_);
                            this.closePlaceholder(state, partStartIndex, true);
                            partStartIndex = index+1;
                            continue;
                        }
                        if (isSpecial) throw new UnexpectedSpecialCharacterException(template, index, char_);
                    }
                    state.specialPartBuilder.append(char_);
                    state.placeholderIdBuilder.append(char_);
                }
                case FILLING_ARGUMENT -> {
                    if (!escapingBuffer.currentlyEscaped()) {
                        if (char_ == this.specialCharacterSet.closing()) {
                            state.specialPartBuilder.append(char_);
                            this.closePlaceholder(state, partStartIndex, true);
                            partStartIndex = index+1;
                            continue;
                        }
                    }
                    state.specialPartBuilder.append(char_);
                    state.argumentBuilder.append(char_);
                }
            }
        }
        switch (state.filling) {
            case FILLING_TEXT -> { if (!state.textBuffer.isEmpty()) state.parsedGST.add(new Text(partStartIndex, state.textBuffer.toString())); }
            case FILLING_ID, FILLING_ARGUMENT -> this.closePlaceholder(state, partStartIndex, false);
        }
        return state.parsedGST;
    }
    
    private void closePlaceholder(ParseState state, int index, boolean endExpected) {
        state.parsedGST.add(new Placeholder(
            new DirectPlaceholderData(
                state.placeholderIdBuilder.toString(),
                state.hasArgument ?
                    Optional.of(state.argumentBuilder.toString()) :
                    Optional.empty(),
                new Text(
                    index,
                    state.specialPartBuilder.toString()
                )
            ),
            state.fillingExclusion,
            endExpected
        ));
        state.nextPart(FillState.FILLING_TEXT);
    }
    
    private enum FillState {
        
        FILLING_TEXT,
        FILLING_ID,
        FILLING_ARGUMENT
        
    }
    
    private static class ParseState {
        
        public LinkedObjects<GSTPart> parsedGST;
        
        public FillState filling;
        public StringBuilder textBuffer;
        public StringBuilder placeholderIdBuilder;
        public StringBuilder argumentBuilder;
        public StringBuilder specialPartBuilder;
        public boolean fillingExclusion;
        public boolean hasArgument;
        
        public ParseState() {
            this.parsedGST = new LinkedObjects<>();
            this.nextPart(FillState.FILLING_TEXT);
        }
        
        public void nextPart(FillState nextFilling) {
            this.filling = nextFilling;
            this.textBuffer = new StringBuilder();
            this.placeholderIdBuilder = new StringBuilder();
            this.argumentBuilder = new StringBuilder();
            this.specialPartBuilder = new StringBuilder();
            this.fillingExclusion = false;
            this.hasArgument = false;
        }
        
    }
    
}