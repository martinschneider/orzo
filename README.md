![Orzo](logo.png)

Orzo is a Java-based language for the JVM.

It's named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) while coding it.

# Features

✨ Orzo-only features (not available in Java)

Orzo compiles a large subset of Java (enough to compile itself, see [Metrics](#metrics)). Unchecked items are not supported (yet); see [docs/TODO.md](docs/TODO.md) for the known gaps in detail.

## Types and declarations

- [X] [primitive types (byte, short, int, char, boolean, long, float, double)](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-2.html#jvms-2.3), incl. binary numeric promotion (e.g. `int + long` is a `long`)
- [X] String
- [X] one-dimensional [arrays](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-3.html#jvms-3.9), array initializers `new int[] {1, 2}` and `int[] a = {1, 2}`, array class literals `int[].class`
- [ ] multi-dimensional arrays
- [ ] C-style array declarations of locals and fields (`int a[]`; C-style parameters `String args[]` are supported)
- [X] classes with fields, constructors (`this(...)`, `super(...)`), static and instance initializers, static initializer blocks `static { }`
- [X] interfaces (implemented by classes of the same compilation unit or the JDK)
- [ ] `default` and `static` interface methods
- [ ] class inheritance (partially supported: `extends` with inherited fields and methods and virtual dispatch work; `super.method()` calls and `abstract` classes don't)
- [X] [enums](https://docs.oracle.com/javase/specs/jls/se18/html/jls-8.html#jls-8.9) incl. constructors with any number of arguments, fields, methods, `values()`, `valueOf()`, `name()`, `ordinal()`
- [X] static nested classes, interfaces and enums (no `InnerClasses`/`NestHost` attributes yet)
- [ ] inner (non-static), local and anonymous classes
- [ ] records
- [ ] generics (partially supported: type arguments of locals, parameters and fields are tracked for calls on JDK generic types, e.g. `list.get(i).length()`, generic calls are typed from their target, e.g. `list.sort(Comparator.comparingInt(v -> v.idx))`, and generic types are emitted as `Signature` attributes; generic classes of your own and calls on variables of a type variable are not supported)
- [ ] `var`
- [X] varargs methods
- [X] static imports (of JDK classes and classes of the same compilation unit)
- [X] autoboxing and unboxing
- [ ] annotations (annotations without arguments are ignored; annotations with arguments are not supported)

## Operators

- [X] assignment `=`, also as an expression (e.g. `while ((line = next()) != null)`)
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment) ✨
- [X] parallel declarations, e.g. `int a,b,c,d,e,f = 1,2,3` ✨
- [X] arithmetic `+`, `-`, `*`, `/`, `%`
- [X] `**` power operator ✨, e.g. `int b = a ** 5` (binds tighter than `*`, left-associative)
- [X] `√` sqrt operator ✨, e.g. `double x = √n + √(n+1)`
- [X] parenthesis `(` ,`)`
- [X] unary post/pre-increment and decrement `++`, `--` (also of fields and array elements)
- [X] compound assignments `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `^=`, `|=`
- [X] relational `<`, `<=`, `>=`, `>`
- [X] equality `==`, `!=`
- [X] bit `&`, `^`, `|`, `<<`, `>>`, `>>>`
- [X] unary minus `-`, unary plus `+`, unary bitwise `~`
- [X] logical `&&`, `||` (short-circuiting) and non-short-circuiting `&`, `|`, `^` on booleans
- [X] unary logical `!`
- [X] [casting](https://docs.oracle.com/javase/specs/jls/se18/html/jls-15.html#jls-15.16) `()` between primitive types, to reference types and unboxing casts (e.g. `(char) obj`)
- [ ] casts to array types, e.g. `(int[]) obj`
- [X] [object creation](https://docs.oracle.com/javase/specs/jls/se18/html/jls-12.html#jls-12.5) `new`
- [X] `instanceof`
- [ ] `instanceof` patterns (`o instanceof String s`)
- [X] String concatenation `+`
- [ ] a comparison as an operand of a string concatenation, e.g. `"x: " + (a == b)` (assign it to a variable first)
- [X] ternary `?:`
- [X] lambdas with an expression body, e.g. `x -> x.name()`
- [ ] lambdas with a block body (`x -> { ... }`) or for a `void` functional interface (e.g. `Consumer`)
- [X] method references to static methods and unbound instance methods, e.g. `Math::min`, `String::length`
- [ ] bound and constructor method references (`obj::method`, `Foo::new`)

## Control structures

- [X] [if, else if and else](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.9)
- [X] [while](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.12)
- [X] [do](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.13)
- [X] [for](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.14)
- [X] [enhanced for](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.14.2) over arrays and `Iterable`s
- [X] [break](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.15) and [continue](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.16), also with labels
- [X] blocks `{ }` and labeled blocks
- [X] [return](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.17)
- [X] method and constructor calls, incl. overload resolution
- [X] [try](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.20) with catch (incl. multi-catch `catch (A | B e)`), finally and try-with-resources (finally isn't run when `break`/`continue` leave the try block)
- [X] `throw`
- [X] [switch](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.11) statements on integral types, `char`, String and enums, incl. fall-through
- [ ] `switch` with `->` cases and switch expressions
- [ ] `assert`, `synchronized`
- [X] [repeat](https://aroberge.github.io/ideas/docs/html/repeat.html) ✨
- [X] [unless](https://www.perltutorial.org/perl-unless/) ✨

## Notes

- Orzo creates class files with major version 71 (Java 27) and emits a [StackMapTable attribute](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4) for methods with branch targets, as required by the JVM spec.
- Classes the compiled code uses are resolved by reflection on Orzo's own classpath (the JDK and Orzo itself) or are part of the same compilation run; there is no `-classpath` option yet.

# Example

Calculating π using the [Gauss-Legendre algorithm](https://en.wikipedia.org/wiki/Gauss%E2%80%93Legendre_algorithm):

```
public double pi(int n) {
  double a, b, t, p, x = 1, 1/√2, 1/4, 1;
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

# Metrics

Measured by `mvn clean verify -Pselfcompile` (see `selfcompile.sh`): Orzo compiles its own sources three times, the first time with the javac-built compiler, then with the Orzo-built one. Compile time is the first pass (one JVM run each, so JVM start-up is included); class size is the class files of the last pass.

<!-- metrics:start -->
| Metric | Value |
|--------|-------|
| Self-compilation (share of source lines compiled by Orzo) | ████████████████████████████████████████ 100.0% |
| Compile time (compiling Orzo's sources) | 4.7x javac (19.0 s vs. 4.1 s) |
| Class size (Orzo's class files) | 4.4x javac (3780.6 KiB vs. 861.4 KiB) |
<!-- metrics:end -->
