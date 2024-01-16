package ru.ancap.gst;

import lombok.SneakyThrows;
import org.apache.commons.rng.core.source64.XoRoShiRo128Plus;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.ancap.commons.Pair;
import ru.ancap.gst.buffer.conf.ConfGSTTerminator;
import ru.ancap.gst.parser.GSTParser;
import ru.ancap.gst.parser.gst_structure.GSTPart;
import ru.ancap.gst.parser.gst_structure.Placeholder;
import ru.ancap.gst.parser.simple.SimpleGSTParser;
import ru.ancap.gst.util.LinkedObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(value = Scope.Benchmark)

// big testing time required due to active usage of baselines
@Warmup(iterations = 3, time = 10000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 30000, timeUnit = TimeUnit.MILLISECONDS)
@Fork(5)
public class GSTBenchmark {
    
    private static final Placeholder DUMMY_PLACEHOLDER = Placeholder.DUMMY;
    
    private final int BASE_LENGTH = 700;
    private final int SAMPLES_AMOUNT = 100;
    private final int SAMPLES_AMOUNT_LARGE = 1000;
    private final int VALUE_SAMPLES = 10000;
    
    private final XoRoShiRo128Plus random = new XoRoShiRo128Plus(System.nanoTime(), this.getClass().hashCode());
    private final char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    
    private final GSTParser parser = SimpleGSTParser.inst();
    
    private final Map<String, String> testHashMap = new HashMap<>();
    
    private final List<Pair<String, List<String>>> simpleSamplesSmall     = new ArrayList<>();
    private final List<Pair<String, List<String>>> simpleSamples          = new ArrayList<>();
    private final List<Pair<String, List<String>>> size10Samples          = new ArrayList<>();
    private final List<Pair<String, List<String>>> size100Samples         = new ArrayList<>();
    private final List<Pair<String, List<String>>> placeholders10Samples  = new ArrayList<>();
    private final List<Pair<String, List<String>>> placeholders100Samples = new ArrayList<>();
    
    private final List<String> valueSamples = new ArrayList<>();
    
    @Setup
    @SneakyThrows
    public void setup() {
        for (int i = 0; i < this.SAMPLES_AMOUNT; i++) {
            this.simpleSamplesSmall.add(this.generateTestString(1, 1));
        }
        for (int i = 0; i < this.SAMPLES_AMOUNT_LARGE; i++) {
            this.simpleSamples          .add(this.generateTestString(1,   1   ));
            this.size10Samples          .add(this.generateTestString(10,  1   ));
            this.size100Samples         .add(this.generateTestString(100, 1   ));
            this.placeholders10Samples  .add(this.generateTestString(1,   10  ));
            this.placeholders100Samples .add(this.generateTestString(1,   100 ));
        }
        for (int i = 0; i < this.VALUE_SAMPLES; i++) {
            this.valueSamples.add(this.generateRandomString(5));
        }
        Thread.sleep(10); System.out.println();
        System.out.println("---------------");
        System.out.println("---------------");
        System.out.println("---------------");
        this.printSomeRandomOf(this.simpleSamples);
        this.printSomeRandomOf(this.placeholders10Samples);
        this.printSomeRandomOf(this.placeholders100Samples);
    }
    
    private void printSomeRandomOf(List<Pair<String, List<String>>> samples) {
        for (int i = 0; i < 1; i++) {
            var sample = samples.get(this.random.nextInt(samples.size()));
            System.out.println("gst = "+sample.key());
            System.out.println("placeholders = "+ String.join(";", sample.value()));
        }
        System.out.println("end");
    }
    
    public Pair<String /*testString*/, List<String> /*associatedPlaceholders*/> generateTestString(int size, int placeholdersAmount) {
        int length = size * this.BASE_LENGTH;
        String randomString = this.generateRandomString(length);
        LinkedString string = new LinkedString(randomString);
        List<String> placeholders = new ArrayList<>(Math.max(10, (int) (placeholdersAmount * 1.10)));
        
        int placeholdersLeft = placeholdersAmount;
        
        LinkedString.Node next = string.base;
        while (next != null) {
            LinkedString.Node this_ = next;
            replace: if (placeholdersLeft > 0) {
                Pair<String /*placeholder*/, String /*excludedKey*/> placeholder = this.generatePlaceholder();
                next = this_.next(7);
                if (next == null) {
                    next = this_.next;
                    break replace;
                }
                placeholders.add(placeholder.value());
                this_.content = placeholder.key();
                this_.next = next;
                next.previous = this_;
                placeholdersLeft--;
            } else {
                next = this_.next;
            }
        }
        return new Pair<>(string.toString(), placeholders);
    }
    
    private long placeholderIndex = 0;
    
    private Pair<String /*placeholder*/, String /*excludedKey*/> generatePlaceholder() {
        String key = Long.toString(this.placeholderIndex, 36);
        if (key.length() > 5) throw new IllegalStateException("length = "+key.length());
        key = String.format("%-5s", key);
        if (key.length() != 5) throw new IllegalStateException("length = "+key.length());
        try {return new Pair<>("\\{"+key+"}", key);} 
        finally {this.placeholderIndex++;}
    }
    
    /**
     * Benchmark overhead:<br>
     * 2x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence simple_small() {
        return this.testBySamples(this.simpleSamplesSmall);
    }
    
    /**
     * Benchmark overhead:<br>
     * 2x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence simple() {
        return this.testBySamples(this.simpleSamples);
    }
    
    /**
     * Expected O(n)<br>
     * Benchmark overhead:<br>
     * 2x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence complexity_size_10() {
        return this.testBySamples(this.size10Samples);
    }
    
    /**
     * Expected O(n)<br>
     * Benchmark overhead:<br>
     * 2x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence complexity_size_100() {
        return this.testBySamples(this.size100Samples);
    }
    
    /**
     * Benchmark overhead:<br>
     * 11x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence complexity_placeholders_10() {
        return this.testBySamples(this.placeholders10Samples);
    }
    
    /**
     * Benchmark overhead: <br>
     * 101x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public CharSequence complexity_placeholders_100() {
        return this.testBySamples(this.placeholders100Samples);
    }
    
    /**
     * Benchmark overhead: <br>
     * 1x {@code : this.bufferInstantiation()}<br>
     * 201x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public void onlyPlaceholders_100(Blackhole blackhole) {
        var buffer = ConfGSTTerminator.newStrict().build();
        var samples = this.placeholders100Samples;
        var sample = samples.get(this.random.nextInt(samples.size()));
        
        for (String placeholderKey : sample.value()) {
            buffer.declare(placeholderKey, (ph) -> this.randomSampledValueString());
            //noinspection DataFlowIssue
            blackhole.consume(buffer.findDeclaration(placeholderKey).handle(GSTBenchmark.DUMMY_PLACEHOLDER));
        }
    }
    
    /**
     * Benchmark overhead: <br>
     * 1x {@code : this.bufferInstantiation()}<br>
     * 21x {@code : this.randomValueOfList()}<br>
     */
    @Benchmark
    public void onlyPlaceholders_10(Blackhole blackhole) {
        var buffer = ConfGSTTerminator.newStrict().build();
        var samples = this.placeholders10Samples;
        var sample = samples.get(this.random.nextInt(samples.size()));
        
        for (String placeholderKey : sample.value()) {
            buffer.declare(placeholderKey, (ph) -> this.randomSampledValueString());
            //noinspection DataFlowIssue
            blackhole.consume(buffer.findDeclaration(placeholderKey).handle(GSTBenchmark.DUMMY_PLACEHOLDER));
        }
    }
    
    @Benchmark
    public ConfGSTTerminator bufferInstantiation() {
        return ConfGSTTerminator.newStrict().build();
    }
    
    @Benchmark
    public String randomValueOfList() {
        return this.valueSamples.get(this.random.nextInt(this.VALUE_SAMPLES));
    }
    
    private String randomSampledValueString() {
        return this.valueSamples.get(this.random.nextInt(this.VALUE_SAMPLES));
    }
    
    private String generateRandomString(int length) {
        char[] strChars = new char[length];
        for(int i = 0; i < length; i++) {
            strChars[i] = this.alphabet[this.random.nextInt(this.alphabet.length)];
        }
        return new String(strChars);
    }
    
    public CharSequence testBySamples(List<Pair<String, List<String>>> samples) {
        var buffer = ConfGSTTerminator.newStrict().build();
        var sample = samples.get(this.random.nextInt(samples.size()));
        
        for (String placeholderKey : sample.value()) {
            buffer.declare(placeholderKey, ignored -> "yoba");
        }
        
        LinkedObjects<GSTPart> parsedGST = this.parser.parse(sample.key());
        return buffer.terminate(parsedGST);
    }
    
}