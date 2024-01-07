package ru.ancap.gst;

import org.junit.jupiter.api.Test;
import ru.ancap.gst.parser.EscapingMode;
import ru.ancap.gst.parser.GSTParser;
import ru.ancap.gst.parser.SpecialCharacterSet;
import ru.ancap.gst.parser.gst_structure.DirectPlaceholderData;
import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.parser.gst_structure.Placeholder;
import ru.ancap.gst.parser.gst_structure.Text;
import ru.ancap.gst.parser.simple.SimpleGSTParser;
import ru.ancap.gst.parser.simple.exception.RegularCharacterEscapedException;
import ru.ancap.gst.parser.simple.exception.UnexpectedSpecialCharacterException;
import ru.ancap.gst.util.LinkedObjects;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleGSTParserTest {
    
    @Test
    public void simple() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.empty(), new Text(3, "\\{bar}")), false, true));
        expected.add(new Text(9, "baz"));
        
        SimpleGSTParser parser = SimpleGSTParser.inst();
        
        assertEquals(expected, parser.parse("foo\\{bar}baz"));
    }

    @Test
    public void nonLatin() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "эава"));
        expected.add(new Placeholder(new DirectPlaceholderData("ххц\uD83D\uDC72\uD83C\uDFFF\uD83D\uDC73\uD83C\uDFFF222", Optional.empty(), new Text(4, "\\{ххц\uD83D\uDC72\uD83C\uDFFF\uD83D\uDC73\uD83C\uDFFF222}")), false, true));
        expected.add(new Text(21, "ззз\uD83D\uDC72\uD83C\uDFFF\uD83D\uDC73\uD83C\uDFFFзу"));

        SimpleGSTParser parser = SimpleGSTParser.inst();

        assertEquals(expected, parser.parse("эава\\{ххц\uD83D\uDC72\uD83C\uDFFF\uD83D\uDC73\uD83C\uDFFF222}ззз\uD83D\uDC72\uD83C\uDFFF\uD83D\uDC73\uD83C\uDFFFзу"));
    }
    
    @Test
    public void exclusion() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.empty(), new Text(3, "\\{!bar}")), true, true));
        expected.add(new Text(10, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{!bar}baz"));
    }
    
    @Test
    public void argument() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("fizz"), new Text(3, "\\{bar:fizz}")), false, true));
        expected.add(new Text(14, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{bar:fizz}baz"));
    }
    
    @Test
    public void complexArgument() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("!-4559{}6!89}56"), new Text(3, "\\{bar:!-4559{\\}6!89\\}56}")), false, true));
        expected.add(new Text(27, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{bar:!-4559{\\}6!89\\}56}baz"));
    }
    
    @Test
    public void illegalName() {
        GSTParser parser = SimpleGSTParser.inst();
    
        String template = "Text \\{{var}}";
        assertThrows(UnexpectedSpecialCharacterException.class, () -> parser.parse(template));
    }
    
    @Test
    public void regularCharacterEscaped() {
        GSTParser parser = SimpleGSTParser.inst();
        String template = "\\Text";
        assertThrows(RegularCharacterEscapedException.class, () -> parser.parse(template));
    }
    
    @Test
    public void unexpectedEnd() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.empty(), new Text(3, "\\{bar")), false, false));
    
        assertEquals(expected, SimpleGSTParser.inst().parse("foo\\{bar"));
    }
    
    @Test
    public void unexpectedEndArgument() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(3, "\\{bar:arg")), false, false));
    
        assertEquals(expected, SimpleGSTParser.inst().parse("foo\\{bar:arg"));
    }
    
    @Test
    public void endOnPlaceholder() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.empty(), new Text(3, "\\{bar}")), false, true));
    
        assertEquals(expected, SimpleGSTParser.inst().parse("foo\\{bar}"));
    }
    
    @Test
    public void onlyText() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
    
        assertEquals(expected, SimpleGSTParser.inst().parse("foo"));
    }
    
    @Test
    public void empty() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
    
        assertEquals(expected, SimpleGSTParser.inst().parse(""));
    }
    
    @Test
    public void alternateSettings() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("fizz]"), new Text(3, "[bar_fizz+]]")), false, true));
        expected.add(new Text(15, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.builder()
            .specialCharacterSet(SpecialCharacterSet.builder()
                .closure('[', ']')
                .argumentDelimiter('_')
                .exclusionChar('!')
                .escapingCharacter('+').build())
            .escapingMode(EscapingMode.UNESCAPED_IS_PLACEHOLDER).build();
        assertEquals(expected, parser.parse("foo[bar_fizz+]]baz"));
    }

    @Test
    public void unescapedWhenRequired() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo{bar}baz"));

        SimpleGSTParser parser = SimpleGSTParser.inst();

        assertEquals(expected, parser.parse("foo{bar}baz"));
    }
    
    @Test
    public void alternateSettingsSameOpeningAndClosing() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("fizz%"), new Text(3, "%bar_fizz+%%")), false, true));
        expected.add(new Text(15, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.builder()
            .specialCharacterSet(SpecialCharacterSet.builder()
                .closure('%', '%')
                .argumentDelimiter('_')
                .escapingCharacter('+').build())
            .escapingMode(EscapingMode.UNESCAPED_IS_PLACEHOLDER).build();
    
        assertEquals(expected, parser.parse("foo%bar_fizz+%%baz"));
    }
    
    @Test
    public void escaping() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("ba}r", Optional.empty(), new Text(3, "\\{ba\\}r}")), false, true));
        expected.add(new Text(11, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{ba\\}r}baz"));
    }
    
    @Test
    public void doubleEscaping() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar\\", Optional.empty(), new Text(3, "\\{bar\\\\}")), false, true));
        expected.add(new Text(11, "baz"));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{bar\\\\}baz"));
    }
    
    @Test
    public void tripleEscapingUnexpectedEnd() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar\\}baz", Optional.empty(), new Text(3, "\\{bar\\\\\\}baz")), false, false));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{bar\\\\\\}baz"));
    }
    
    /////
    
    @Test
    public void endOnPlaceholderExclusionArgument() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Text(0, "foo"));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(3, "\\{!bar:arg}")), true, true));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("foo\\{!bar:arg}"));
    }
    
    @Test
    public void onlyPlaceholdersOneKey() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(0, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(10, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(20, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(30, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(40, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(50, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(60, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(70, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(80, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(90, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(100, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(110, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(120, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(130, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(140, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(150, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(160, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(170, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(180, "\\{bar:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar", Optional.of("arg"), new Text(190, "\\{bar:arg}")), false, true));
    
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}\\{bar:arg}"));
    }
    
    @Test
    public void onlyPlaceholdersDifferentKeysEndUnexpectedOneExclusion() {
        LinkedObjects<GSTPart> expected = new LinkedObjects<>();
        expected.add(new Placeholder(new DirectPlaceholderData("bar1", Optional.of("arg"), new Text(0, "\\{bar1:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar2", Optional.of("arg"), new Text(11, "\\{bar2:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar3", Optional.of("arg"), new Text(22, "\\{bar3:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar4", Optional.of("arg"), new Text(33, "\\{bar4:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar5", Optional.of("arg"), new Text(44, "\\{!bar5:arg}")), true, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar6", Optional.of("arg"), new Text(56, "\\{bar6:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar7", Optional.of("arg"), new Text(67, "\\{bar7:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar8", Optional.of("arg"), new Text(78, "\\{bar8:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar9", Optional.of("arg"), new Text(89, "\\{bar9:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar10", Optional.of("arg"), new Text(100, "\\{bar10:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar11", Optional.of("arg"), new Text(112, "\\{bar11:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar12", Optional.of("arg"), new Text(124, "\\{bar12:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar13", Optional.of("arg"), new Text(136, "\\{bar13:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar14", Optional.of("arg"), new Text(148, "\\{bar14:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar15", Optional.of("arg"), new Text(160, "\\{bar15:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar16", Optional.of("arg"), new Text(172, "\\{bar16:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar17", Optional.of("arg"), new Text(184, "\\{bar17:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar18", Optional.of("arg"), new Text(196, "\\{bar18:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar19", Optional.of("arg"), new Text(208, "\\{bar19:arg}")), false, true));
        expected.add(new Placeholder(new DirectPlaceholderData("bar20", Optional.of("arg"), new Text(220, "\\{bar20:arg")), false, false));
        
        SimpleGSTParser parser = SimpleGSTParser.inst();
    
        assertEquals(expected, parser.parse("\\{bar1:arg}\\{bar2:arg}\\{bar3:arg}\\{bar4:arg}\\{!bar5:arg}\\{bar6:arg}\\{bar7:arg}\\{bar8:arg}\\{bar9:arg}\\{bar10:arg}\\{bar11:arg}\\{bar12:arg}\\{bar13:arg}\\{bar14:arg}\\{bar15:arg}\\{bar16:arg}\\{bar17:arg}\\{bar18:arg}\\{bar19:arg}\\{bar20:arg"));
    }
    
}
