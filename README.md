Universal and efficient way to handle placeholders in strings in Java.

### It can be simple...

```java
GSTParser parser = SimpleGSTParser.inst();

var buffer = ConfGSTTerminator.newStrict().build();
buffer.declare("foo", "fizz");
buffer.declare("bar", "buzz");
        
String result = buffer.terminate(parser.parse("foo = \\{foo}; bar = \\{bar}")); // output is "foo = fizz; bar = buzz"
```

### Or fine-tuned...

```java
GSTParser parser = SimpleGSTParser.builder()
    .specialCharacterSet(SpecialCharacterSet.builder()
        .closure('(', ')')
        .argumentDelimiter('_').build())
    .escapingMode(EscapingMode.UNESCAPED_IS_PLACEHOLDER).build();

var buffer = ConfGSTTerminator.newLenient().build(); // fail-safe

buffer.declare("isPrime", (placeholder) -> ""+ Primes.isPrime(Integer.parseInt(placeholder.argument().orElseThrow())));

String result1 = buffer.terminate(parser.parse("(isPrime_14); (isPrime_7)"   )); // output is "false; true"
String result2 = buffer.terminate(parser.parse("\\(isPrime_14); (isPrime_7)" )); // output is "(isPrime_14); true"
String result3 = buffer.terminate(parser.parse("(isPrime_foo); (isPrime_7)"  )); // output is "(isPrime_foo); true"
String result4 = buffer.terminate(parser.parse("(isPrime_foo); (isPrime_7"   )); // output is "(isPrime_foo); true"
```

### Benchmark and performance
Benchmarks are located in gst-benchmark module and can be ran with ```java -jar bench-jmh.jar``` after ```mvn clean install``` with at least Java 21. In general, (on my processor) one placeholder in string consumes 80 nanoseconds (parse + buffer + termination overheads) and one symbol consumes 7 nanoseconds with linear complexity both from placeholders and symbols. 

Parse result can be cached as LinkedObjects<GSTPart>. Be careful with caching though, it can easily from optimization become deoptimization.

Benchmark results:

```
Benchmark                                               Mode  Cnt       Score      Error  Units
GSTBenchmark.bufferInstantiation                        avgt   25       6,071 ?    0,007  ns/op
GSTBenchmark.complexity_placeholders_10                 avgt   25    5854,903 ?  107,804  ns/op
GSTBenchmark.complexity_placeholders_100                avgt   25   13461,520 ?   72,909  ns/op
GSTBenchmark.complexity_size_10                         avgt   25   42195,679 ?   40,670  ns/op
GSTBenchmark.complexity_size_100                        avgt   25  401701,099 ? 2334,291  ns/op
GSTBenchmark.onlyPlaceholders_10                        avgt   25     292,847 ?    2,479  ns/op
GSTBenchmark.onlyPlaceholders_100                       avgt   25    2947,740 ?   17,754  ns/op
GSTBenchmark.randomValueOfList                          avgt   25       2,905 ?    0,014  ns/op
GSTBenchmark.simple                                     avgt   25    4987,801 ?  178,821  ns/op
GSTBenchmark.simple_small                               avgt   25    5063,460 ?  193,372  ns/op
```

### Maven

```xml
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>

        <dependency>
            <groupId>com.github.PukPukov.gst</groupId>
            <artifactId>gst-core</artifactId>
            <version>look above</version>
            <scope>compile</scope>
        </dependency>
```
