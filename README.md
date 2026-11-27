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
- method references `this::m`, `super::m` and `Foo::new`
- `switch` with `->` cases and switch expressions
- `assert`, `synchronized`
- `finally` isn't run when `break` or `continue` leave the `try` block

## Notes

- Orzo creates class files with major version 71 (Java 27) and emits a [StackMapTable attribute](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4) for methods with branch targets, as required by the JVM spec.
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

Numerical calculus with lambdas ([Newton's method](https://en.wikipedia.org/wiki/Newton%27s_method) and [Simpson's rule](https://en.wikipedia.org/wiki/Simpson%27s_rule)):

```java
static DoubleUnaryOperator derivative(DoubleUnaryOperator f) {
  return x -> (f.applyAsDouble(x + h) - f.applyAsDouble(x - h)) ÷ (2 * h);
}

static double root(DoubleUnaryOperator f, double x) {
  DoubleUnaryOperator df = derivative(f);
  repeat 10 {
    x = x - f.applyAsDouble(x) / df.applyAsDouble(x);
  }
  return x;
}

static double integral(DoubleUnaryOperator f, double a, double b, int n) {
  double dx, sum, x = (b - a) ÷ n, f.applyAsDouble(a) + f.applyAsDouble(b), a;
  for (int i = 1; i < n; i++) {
    x += dx;
    sum += (i % 2 == 0 ? 2 : 4) * f.applyAsDouble(x);
  }
  return sum * dx ÷ 3;
}

root(x -> x ** 2 - x - 1, 1);                 // the golden ratio: 1.618033988749895
root(x -> Math.cos(x) - x, 1);                // the Dottie number: 0.7390851332151607
integral(x -> 4 ÷ (1 + x ** 2), 0, 1, 100);   // π: 3.141592653589754
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
| [jtreg](https://openjdk.org/jtreg/) positive tests (must compile and run) | 48.8% (431 of 883) |
| jtreg negative tests (must be rejected) | 56.7% (489 of 863) |
| Compile time (Orzo's sources) | 1.7x javac (19.7 s vs. 11.6 s) |
| Class size (Orzo's class files) | 1.0x javac (1086.0 KiB vs. 1070.8 KiB) |
<!-- metrics:end -->
