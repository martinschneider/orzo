![Progress](https://progress-bar.dev/14/?title=self-compile)

![Orzo](logo.png)

Orzo is a Java-based language for the JVM.

It's named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) while coding it.

# Features

✨ Orzo-only features (not available in Java)

## Types

- [X] [primitive types (byte, short, int, char, boolean, long, float, double)](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-2.html#jvms-2.3)
- [X] one-dimensional [arrays](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-3.html#jvms-3.9)
- [X] String
- [X] interfaces
- [ ] [reference types](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-2.html#jvms-2.4) (partially supported)
- [ ] [enums](https://docs.oracle.com/javase/specs/jls/se18/html/jls-8.html#jls-8.9) (partially supported)
- [ ] class inheritance
- [ ] [exceptions](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-2.html#jvms-2.10)
- [ ] multi-dimensional arrays

## Operators

- [X] assignment `=`
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment) ✨
- [X] parallel declarations, e.g. `int a,b,c,d,e,f = 1,2,3` ✨
- [X] [repeat](https://aroberge.github.io/ideas/docs/html/repeat.html) ✨
- [X] arithmetic `+`, `-`, `*`, `/`, `%`
- [X] `**` power operator ✨, e.g. `int b = a ** 5`
- [X] `√` sqrt operator ✨, e.g. `double x = √n + √(n+1)`
- [X] parenthesis `(` ,`)`
- [X] unary post/pre-increment and decrement `++`, `--`
- [X] compound assignments `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `^=`, `|=`
- [X] relational `<`, `<=`, `>=`, `>`
- [X] equality `==`, `!=`,
- [X] bit `&`, `^`, `|`, `<<`, `>>`, `>>>`
- [X] unary minus `-`
- [ ] [casting](https://docs.oracle.com/javase/specs/jls/se18/html/jls-15.html#jls-15.16) `()` (partially supported)
- [ ] [object creation](https://docs.oracle.com/javase/specs/jls/se18/html/jls-12.html#jls-12.5) `new` (partially supported)
- [ ] logical `&&`, `||`
- [ ] unary logical `!`
- [ ] unary bitwise `~`
- [ ] unary plus `+`
- [ ] `instanceof`
- [ ] String and char concatenation `+`
- [ ] ternary `?:`

## Control strcutures

- [X] [if, else if and else](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.9)
- [X] [while](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.12)
- [X] [do](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.13)
- [X] [for](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.14)
- [X] [break](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.15)
- [X] [return](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.17)
- [ ] method and constructor calls (partially supported)
- [ ] [continue](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.16)
- [ ] [try](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.20)
- [ ] [switch](https://docs.oracle.com/javase/specs/jls/se18/html/jls-14.html#jls-14.11)
- [ ] [lambdas](https://docs.oracle.com/javase/specs/jls/se18/html/jls-15.html#jls-15.27)
- [ ] [unless](https://www.perltutorial.org/perl-unless/) ✨

## Notes

- array defintions must be of the form `int[] a`, `int a[]` is not supported
- Orzo creates class files with major version 50 (Java 6). Newer versions would require implementation of the [StackMapTable attribute](https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.4).
- fields and variables share the same namespace

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
