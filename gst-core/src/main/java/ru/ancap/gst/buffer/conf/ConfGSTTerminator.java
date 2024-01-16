package ru.ancap.gst.buffer.conf;

import lombok.*;
import org.jetbrains.annotations.Nullable;
import ru.ancap.gst.buffer.PlaceholderHandler;
import ru.ancap.gst.buffer.SimpleReplace;
import ru.ancap.gst.buffer.conf.exception.PlaceholderOverrideException;
import ru.ancap.gst.buffer.conf.exception.PlaceholderProcessingException;
import ru.ancap.gst.buffer.conf.exception.PlaceholderSetupToNowhereException;
import ru.ancap.gst.buffer.conf.exception.UnhandledPlaceholderException;
import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.parser.gst_structure.Placeholder;
import ru.ancap.gst.parser.gst_structure.Text;
import ru.ancap.gst.parser.simple.exception.UnexpectedEndOfInputException;
import ru.ancap.gst.util.LinkedObjects;
import ru.ancap.gst.util.Node;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;

@ToString @EqualsAndHashCode
@lombok.Builder(builderClassName = "Builder")
public class ConfGSTTerminator {
    
    private final Map<String, PlaceholderHandler> declarations = new HashMap<>();
    
    @lombok.Builder.Default private final InsertionSafer insertionSafer = InsertionSafer.UNSAFE;
    @lombok.Builder.Default private final PhProcessingErrorHandler phProcessingErrorHandler = (ign, err) -> {
        throw new PlaceholderProcessingException(err);
    };
    @lombok.Builder.Default private final PlaceholderHandler unhandledPlaceholderHandler = unused -> {
        throw new UnhandledPlaceholderException(unused);
    };
    @lombok.Builder.Default private final OptionalHandler<SetupToNowhereHandler> setupToNowhereHandler = OptionalHandler.checking(
        (key, handler) -> {throw new PlaceholderSetupToNowhereException(key, handler);}
    );
    @lombok.Builder.Default private final OptionalHandler<PlaceholderOverrideHandler> placeholderOverrideHandler = OptionalHandler.checking(
        (key, prev, next) -> { throw new PlaceholderOverrideException(key, prev, next); }
    );
    @lombok.Builder.Default private final OptionalHandler<UnexpectedEndOfInputHandler> unexpectedEndOfInputHandler = OptionalHandler.checking(
        gst -> { throw new UnexpectedEndOfInputException(gst);}
    );
    
    /**
     * Generally discouraged to use because of hard bug fixing in case of errors in GST with this terminator.
     */
    public static ConfGSTTerminator.Builder newLenient() {
        return ConfGSTTerminator.builder()
            .phProcessingErrorHandler((ph, ign) -> ph.originalPart().string())
            .unhandledPlaceholderHandler(unused -> unused.directData().originalPart().string())
            .setupToNowhereHandler(new LenientOptionalHandler<>())
            .placeholderOverrideHandler(new LenientOptionalHandler<>())
            .unexpectedEndOfInputHandler(new LenientOptionalHandler<>());
    }
    
    /**
     * Volatile because non-volatile field in this case can possible lead to not dangerous
     * but surely unpleasant bug in multithreaded loaders when first message will sometimes
     * be shown with default formatting
     */
    @Setter
    private static Function<Logger, ConfGSTTerminator.Builder> warningProvider = logger -> ConfGSTTerminator.builder()
        .phProcessingErrorHandler((ph, err) -> {
            logger.throwing(ConfGSTTerminator.class.getName(), "newWarning", new PlaceholderProcessingException(err));
            return ph.originalPart().string();
        })
        .unhandledPlaceholderHandler(unused -> {
            logger.throwing(ConfGSTTerminator.class.getName(), "newWarning", new UnhandledPlaceholderException(unused));
            return unused.directData().originalPart().string();
        })
        .setupToNowhereHandler(OptionalHandler.checking((key,handler) -> {
            logger.throwing(ConfGSTTerminator.class.getName(), "newWarning", new PlaceholderSetupToNowhereException(key, handler));
        }))
        .placeholderOverrideHandler(OptionalHandler.checking((key, prev, next) -> {
            logger.throwing(ConfGSTTerminator.class.getName(), "newWarning", new PlaceholderOverrideException(key, prev, next));
            return true;                                                                                                         
        }))
        .unexpectedEndOfInputHandler(OptionalHandler.checking((gst) -> {
            logger.throwing(ConfGSTTerminator.class.getName(), "newWarning", new UnexpectedEndOfInputException(gst));
        }));
    
    /**
     * Fail-safe (lenient) terminator that still says to user what happened wrong. 
     * Uses Instance pattern since usually warnings shouldn't customize per every
     * call rather than per system.
     */
    public static ConfGSTTerminator.Builder newWarning(Logger logger) {
        return ConfGSTTerminator.warningProvider.apply(logger);
    }
    
    public static ConfGSTTerminator.Builder newStrict() {
        return ConfGSTTerminator.builder();
    }
    
    public void declare(String key, String replacement) {
        this.declare(key, new SimpleReplace(replacement));
    }
    
    public void declare(String key, ConfExpectPlaceholderHandler handler) {
        this.declareRaw(key, new CallConfer(this.insertionSafer, this.phProcessingErrorHandler, handler));
    }
    
    public @Nullable PlaceholderHandler findDeclaration(String placeholderKey) {
        return this.declarations.get(placeholderKey);
    }
    
    @RequiredArgsConstructor
    public static class CallConfer implements PlaceholderHandler {
        
        private final InsertionSafer insertionSafer;
        private final PhProcessingErrorHandler phProcessingErrorHandler;
        
        private final ConfExpectPlaceholderHandler shrinkedCallTarget;
        
        @Override
        public String handle(Placeholder placeholder) {
            try                         { return this.insertionSafer.safe(this.shrinkedCallTarget.handle(placeholder.directData())); }
            catch (Throwable throwable) { return this.phProcessingErrorHandler.handle(placeholder.directData(), throwable);          }
        }
        
    }
    
    public void declareRaw(String key, PlaceholderHandler handler) {
        if (this.placeholderOverrideHandler.checkForProblem()) {
            var prev = this.declarations.get(key);
            if (prev != null) if (!this.placeholderOverrideHandler.handler().handle(key, prev, handler)) return;
        }
        this.declarations.put(key, handler);
    }
    
    public String terminate(LinkedObjects<GSTPart> terminated) {
        return this.terminate(terminated, Set.of());
    }
    
    /**
     * Does not provide side effect. Makes minimal possible amount of calls to hashmaps
     * (ph = 1 remove + 1 get, declaration = 1 put) to ensure
     * max performance.
     */
    public String terminate(LinkedObjects<GSTPart> terminated, Set<String> excludedKeys) {
        StringBuilder result = new StringBuilder();
        if (terminated.graphStart() == null) return "";
        //noinspection DataFlowIssue same reason as in Node#add()
        if (this.unexpectedEndOfInputHandler.checkForProblem() && terminated.graphEnd().contents() instanceof Placeholder placeholder && !placeholder.endExpected()) {
            this.unexpectedEndOfInputHandler.handler().handle(terminated);
        }
        Map<String, PlaceholderHandler> lookup = new HashMap<>(this.declarations);
        Iterator<Node<GSTPart>> iterator = terminated.iterator();
        while (true) {
            if (iterator.hasNext()) {
                Node<GSTPart> node = iterator.next();
                GSTPart part = node.contents();
                switch (part) {
                    case Placeholder placeholder -> {
                        var state = this.declarations.get(placeholder.directData().key());
                        lookup.remove(placeholder.directData().key()); // if you know how to get rid of this call let me know
                        if (placeholder.exclusion()) continue;
                        if (state == null) result.append(this.unhandledPlaceholderHandler.handle(placeholder));
                        else result.append(state.handle(placeholder));
                    }
                    case Text text -> result.append(text.string());
                }
            } else {
                if (this.setupToNowhereHandler.checkForProblem()) for (var setToNowhere : lookup.entrySet()) {
                    if (!excludedKeys.contains(setToNowhere.getKey())) this.setupToNowhereHandler.handler().handle(setToNowhere.getKey(), setToNowhere.getValue());
                }
                break;
            }
        }
        return result.toString();
    }
    
}