![Orzo](logo.png)

Orzo is a Java-based language for the JVM.

It started during a compiler class at University back in 2010. It had a working lexer and some parts of its parser. I re-discovered the code during the COVID pandemic in 2020, added a JVM code generator and got it to compile some simple classes, including parts of its own source code. In 2025, I dug it out a second time to use it as a personal benchmark for new AI models. Claude and I finished self-compilation in 2026. The next steps are full Java-compliance and improvements to speed and class size.

Orzo is named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) ~while coding it~ during parts of its development.

# Features

Orzo compiles a large subset of Java, enough to compile itself (see [Metrics](#metrics)), and adds a few features of its own.

## Orzo-only features ✨

- [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a, b = b, a + b;`
- parallel declarations, e.g. `int a, b, c = 1, 2, 3;`
- `**` power operator, e.g. `int b = a ** 5` (binds tighter than `*`, left-associative)
- `√` square root, e.g. `double x = √n + √(n + 1)`
- `⌊x⌋` floor, e.g. `long n = ⌊x⌋`
- `÷` floating-point division, e.g. `double quarter = 1 ÷ 4` (`/` divides integers like in Java; Orzo warns about an integer division whose result is used as a floating-point value, e.g. `double d = 1 / 4;`)
- [repeat](https://aroberge.github.io/ideas/docs/html/repeat.html), e.g. `repeat n { ... }` (`repeat { ... }` loops forever)
- [unless](https://www.perltutorial.org/perl-unless/), e.g. `unless (done) { ... }`

## Java features that aren't supported (yet)

Everything else works like in Java, except for (see [docs/TODO.md](docs/TODO.md) for details):

- multi-dimensional arrays and C-style array declarations of locals and fields (`int a[]`)
- records
- generics are only partially supported: the type arguments of your own generic classes aren't tracked (e.g. `box.get().length()` on a `Box<String>`)
- `var`, `instanceof` patterns (`o instanceof String s`), casts to array types (`(int[]) obj`)
- annotations with arguments (annotations without arguments are ignored)
- method references `super::m` and `Foo::new`
- `switch` with `->` cases and switch expressions
- `assert`, `synchronized`
- `finally` isn't run when `break` or `continue` leave the `try` block

## Notes

- Orzo creates class files with major version 71 (Java 27).
- Classes the compiled code uses are resolved by reflection on Orzo's own classpath (the JDK and Orzo itself) or are part of the same compilation run; there is no `-classpath` option yet.
- Static nested classes are compiled without `InnerClasses`/`NestHost` attributes.

# Example

Calculating π using the [Gauss-Legendre algorithm](https://en.wikipedia.org/wiki/Gauss%E2%80%93Legendre_algorithm):

```java
public double pi(int n) {
  double a, b, t, p, x = 1, 1/√2, 1÷4, 1;
  repeat n
  {
    x, a, b  =  a, (a + b) / 2, √(a*b);
    t, p     =  t - p * ((x-a) ** 2), 2 * p;
  }
  return ((a+b) ** 2) / (4 * t);
}
```

More examples can be found [here](src/test/resources/io/github/martinschneider/orzo/examples).

# Build

## with mvn

`mvn package`

## with `javac` and `jar`

`javac $(find ./src/main/java -name "*.java") -d bin && jar cfe orzo.jar io.github.martinschneider.orzo.Orzo -C bin .`

# Usage

`java -jar target/orzo.jar inputFiles -d outputFolder`

Errors are reported as `File.java:line:column: error: message` (add `-v <n>` to also see where in the compiler they were detected). If there are errors, no class files are written and Orzo exits with status 1. Warnings (`File.java:line:column: warning: message`) don't stop the compilation.

# Metrics

<!-- metrics:start -->
| Metric | Value |
|--------|-------|
| Self-compilation | 100.0% |
| [jtreg](https://openjdk.org/jtreg/) positive tests (must compile and run) | 50.5% (446 of 883) |
| jtreg negative tests (must be rejected) | 56.5% (488 of 863) |
| Compile time (Orzo's sources) | 1.1x javac (15.0 s vs. 13.3 s) |
| Class size (Orzo's class files) | 1.0x javac (1105.2 KiB vs. 1081.4 KiB) |
<!-- metrics:end -->
