![Orzo](logo.png)

Orzo is a Java-based language for the JVM.

It's named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) while coding it.

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
- `abstract` classes and methods, `default` and `static` interface methods
- inner (non-static), local and anonymous classes, records
- generics are only partially supported: the type arguments of your own generic classes aren't tracked (e.g. `box.get().length()` on a `Box<String>`)
- `var`, `instanceof` patterns (`o instanceof String s`), casts to array types (`(int[]) obj`)
- annotations with arguments (annotations without arguments are ignored)
- lambdas with a block body (`x -> { ... }`) or for a `void` functional interface (e.g. `Consumer`), bound and constructor method references (`obj::method`, `Foo::new`)
- `switch` with `->` cases and switch expressions
- `assert`, `synchronized`
- `finally` isn't run when `break` or `continue` leave the `try` block

## Notes

- Orzo creates class files with major version 71 (Java 27) and emits a [StackMapTable attribute](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4) for methods with branch targets, as required by the JVM spec.
- Classes the compiled code uses are resolved by reflection on Orzo's own classpath (the JDK and Orzo itself) or are part of the same compilation run; there is no `-classpath` option yet.
- Static nested classes are compiled without `InnerClasses`/`NestHost` attributes.

# Example

Calculating π using the [Gauss-Legendre algorithm](https://en.wikipedia.org/wiki/Gauss%E2%80%93Legendre_algorithm):

```
public double pi(int n) {
  double a, b, t, p, x = 1, 1/√2, 1÷4, 1;
  repeat n
  {
    x, a, b  =  a, (a + b) / 2, √(x*b);
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

Besides, self-compilation, we keep track of a few other metrics for each build.

Java compliance is the share of the self-contained javac tests of the [OpenJDK](https://github.com/openjdk/jdk) test suite (`test/langtools/tools/javac`, run by [jtreg](https://github.com/openjdk/jtreg)) that Orzo passes: a valid test program must compile (and run), an invalid one must be rejected. Only the tests that javac passes count. It is measured separately by `compliance/jtreg.sh`.

<!-- metrics:start -->
| Metric | Value |
|--------|-------|
| Self-compilation (share of source lines compiled by Orzo) | 100.0% |
| Compile time (compiling Orzo's sources) | 4.4x javac (25.2 s vs. 5.7 s) |
| Class size (Orzo's class files) | 4.3x javac (4028.6 KiB vs. 935.3 KiB) |
| Java compliance (OpenJDK javac tests, see compliance/jtreg.sh) | 47.4% (827 of 1746 tests: 237 of 883 valid programs, 590 of 863 invalid ones rejected) |
<!-- metrics:end -->
