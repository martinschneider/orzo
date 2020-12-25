![Progress](https://progress-bar.dev/3/?title=self-compile)

![Orzo](logo.png)

Orzo is a Java-based language for the JVM.

It's named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) while coding it.

# Features

✨ Orzo-only features (not available in Java)

## Types
- [X] [primitive types (byte, short, int, char, boolean, long, float, double)](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-2.html#jvms-2.3)
- [X] one-dimensional [arrays](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-3.html#jvms-3.9)
- [ ] multi-dimensional arrays
- [X] String
- [ ] [reference types](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-2.html#jvms-2.4)
- [ ] class inheritance
- [ ] interfaces
- [ ] [enums](https://docs.oracle.com/javase/specs/jls/se14/html/jls-8.html#jls-8.9)
- [ ] [exceptions](https://docs.oracle.com/javase/specs/jvms/se14/html/jvms-2.html#jvms-2.10)

## Operators
- [X] assignment `=`
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment) ✨
- [X] arithmetic `+`, `-`, `*`, `/`, `%`
- [X] `**` power operator ✨, e.g. `int b = a ** 5`
- [X] `√` sqrt operator ✨, e.g. `double x = √n + √(n+1)`
- [X] parenthesis `(` ,`)`
- [X] unary post/pre-increment and decrement `++`, `--`
- [X] compound assignments `+=`, `-=`, `*=`, `/=`, `%=`, `<<=`, `>>=`, `>>>=`, `&=`, `^=`, `|=`
- [X] relational `<`, `<=`, `>=`, `>`
- [X] equality `==`, `!=`,
- [X] bit `&`, `^`, `|`, `<<`, `>>`, `>>>`
- [ ] logical `&&`, `||`
- [ ] unary logical `!`
- [ ] unary bitwise `~`
- [ ] unary plus `+`
- [X] unary minus `-`
- [ ] `instanceof`
- [ ] String and char concatenation `+`
- [ ] ternary `?:`
- [X] cast `()` for basic types
- [ ] object creation `new`

## Statements
- [X] System.out.println
- [X] if, else if and else
- [ ] unless keyword ✨
- [X] while, do, for and break
- [ ] switch
- [ ] lambdas
- [X] static method calls
- [ ] other method calls
- [ ] constructor calls
- [X] parallel declarations, e.g. `int a,b,c,d,e,f = 1,2,3` ✨

## Notes
- array defintions must be of the form `int[] a`, `int a[]` is not supported
- Orzo creates class files with major version 49

# Examples
Calculating π using the [Gauss-Legendre algorithm](https://en.wikipedia.org/wiki/Gauss%E2%80%93Legendre_algorithm):

```
public static double pi(int n) {
  double a, b, t, p, x = 1, 1/√2, 1/4, 1;
  for (int i=0; i<n; i++)
  {
    x, a, b  =  a, (a + b) / 2, √(x*b);
    t, p     =  t - p * ((x-a) ** 2), 2 * p;
  }
  return ((a+b) ** 2) / (4 * t);
}
```

More examples can be found [here](src/test/resources/io/github/martinschneider/orzo/examples).

# Building

## Using mvn
`mvn package`

## Using `javac` and `jar`
`javac $(find ./src/main/java -name "*.java") -d bin && jar cfe orzo.jar io.github.martinschneider.orzo.Orzo -C bin .`

## Using `orzo` (not working yet)
`orzo $(find ./src/main/java -name "*.java") -d bin && jar cfe orzo.jar io.github.martinschneider.orzo.Orzo -C bin .`

# Setup alias (optional)
`alias orzo="java -jar /path/to/orzo.jar"`
The following examples assume the above alias. To usem them without the alias, replace `orzo` with `java -jar orzo.jar`.

# Usage

`orzo inputFiles -d outputFolder`

Note, that orzo will create folders according to the package structure of the input files. For example, the class file for `org.example.demo.HelloWorld` will be written to `outputFolder/org/example/demo/HelloWorld.class`.

# Design considerations
The compiler is split into three parts: [Lexer](src/main/java/io/github/martinschneider/orzo/lexer/Lexer.java), [Parser](src/main/java/io/github/martinschneider/orzo/parser/Parser.java) and [Code Generator](src/main/java/io/github/martinschneider/orzo/codegen/CodeGenerator.java).

It only uses Java core libraries and has no external dependencies (except for unit testing which requires [JUnit](https://junit.org).

To make the code less verbose, public fields and static imports are heavily used. This might not be considered best practice for other Java projects but it serves its purpose well for this one.

## Lexer
The Lexer uses a [`PushbackReader`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/io/PushbackReader.html) with a small wrapper to keep track of line numbers (for error handling).

Its input is a [`File`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/io/File.html) and it returns a [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java).

### Parser

The Parser takes a [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) and produces a list of [`Clazz`](src/main/java/io/github/martinschneider/orzo/parser/prdocutions/Clazz.java) objects which represent the AST of the program.

A [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) has a pointer which keeps track of the current token processed by the parser.

There is one implementation of the [`ProdParser`](src/main/java/io/github/martinschneider/orzo/parser/ProdParser.java) interface [for every production](src/main/java/io/github/martinschneider/orzo/parser) in the grammar. Its `parse` method will return a production if it matches and `null` otherwise. It must also set the pointer in the [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) accordingly:

> If there is a match, the token index must point to the next token after the match. If there is no match, the token index must be restored to the val before `parse` has been called.

This allows passing the [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) through the different sub-parsers.

We use a simple dependency injection pattern to call different [`ProdParser`](src/main/java/io/github/martinschneider/orzo/parser/ProdParser.java) implementations from each other. Each class has an instance of [`ParserContext`](src/main/java/io/github/martinschneider/orzo/parser/ParserContext.java) which has references to all parsers.

### Code generator

The Code generator takes the AST returned by the parser and outputs a byte array (wrapped inside an [`Output`](src/main/java/io/github/martinschneider/orzo/codegen/Output.java) object) which is then written to a JVM class file.

Similarly to the parser, there is one implementation of [`StatementGenerator`](src/main/java/io/github/martinschneider/orzo/codegen/generators/StatementGenerator.java) [for each statement type](src/main/java/io/github/martinschneider/orzo/codegen/generators) and a similar dependency injection concept (see [`CGContext`](src/main/java/io/github/martinschneider/orzo/codegen/CGContext.java)). Besides, there are some shared generators for basic operations.

### Tests

High coverage with meaningful tests is crucial for a project like this. There are several types of tests:

 - Lexer: [Unit tests](src/test/java/io/github/martinschneider/orzo/lexer) for each token type
 - Parser: [Unit tests](src/test/java/io/github/martinschneider/orzo/parser) for each production type
 - Code Generator: [Unit tests](src/test/java/io/github/martinschneider/orzo/codegen) for each production/statement type (mostly missing at the moment)
 - [Integration tests](src/test/java/io/github/martinschneider/orzo/OrzoTest.java) compiling [sample programs](src/test/resources/io/github/martinschneider/orzo/tests) using Orzo and verifying their output against [predefined expectations](src/test/resources/io/github/martinschneider/orzo/tests/output)
 - Bytecode regression tests: These tests check whether the bytecode created by the integration tests matches the one of the previous version (the baseline). This can be useful to check whether changes to the code generation have any unexpected side effects. Note: I consider this the least important test type, it is a nice-to-have on top of unit and integration tests.
 - Self-compilation tests (Phase 1): These tests check whether Orzo can successfully compile its own source code. After running the unit tests, we will re-compile [all files that Orzo can already handle](whitelist.txt) and then re-run only the integration tests (see above) in the `integration-test` phase of the Maven build (this is configured in the `selfcompile` profile): `mvn verify -Pselfcompile`. 
 - Self-compilation tests (Phase 2): Once Orzo can compile its entire source-code, the next check will be to verify that the self-compiled compiler produces the same bytecode as the one compiled with `javac`.
