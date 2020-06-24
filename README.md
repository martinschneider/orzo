![Orzo](logo.png)

Orzo is a (not yet) self-compiling Java compiler with some added syntactical sugar (for example, [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment) support).

It's named after [my beverage of choice](http://thecoffeeuniverse.org/caffe-dorzo-barley) while coding it ☕🌾

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
- [ ] cast `()`
- [ ] object creation `new`

## Statements
- [X] System.out.println
- [X] if, else if and else statements
- [X] while, do and for loops + break statement
- [ ] switch
- [ ] lambdas
- [X] static method calls
- [ ] other method calls
- [ ] constructor calls
- [ ] parallel declarations, e.g. `int a,b,c,d,e,f = 1,2,3` ✨

## Notes
- array defintions must be of the form `int[] a`, `int a[]` is not supported
- Orzo creates class files targeting JDK 5 (major version 49) or newer

# Examples
Working examples can be found [here](src/test/resources/io/github/martinschneider/orzo/examples).

# Building

## Using mvn
`mvn package`

## Using `javac` and `jar`
`javac $(find ./src/main/java -name "*.java") -d bin && jar cfe orzo.jar io.github.martinschneider.orzo.Orzo -C bin .`

## Using `orzo` (not working yet)
`orzo $(find ./src/main/java -name "*.java") -d bin && jar cfe orzo.jar io.github.martinschneider.orzo.Orzo -C bin .`

# Setup alias (optional)
`alias orzo="java -jar /path/to/orzo.jar"`
The following examples assume the above alias. If you don't have it, replace `orzo` with `java -jar orzo.jar`.

# Usage

To compile `HelloWorld.java` run `orzo HelloWorld.java`. This will generate a class file based on the package and class names defined in `HelloWorld.java`.

For example, the following Java program will be compiled into a class file at `com/examples/HelloWorld.class`.

```
package com.examples;

public class HelloWorld {
  public static void main(String[] args) {
    System.out.println("Hello world");
  }
}
```

The root of the output folder is the current directory. This can be changed using the `-d` option. For example, `orzo HelloWorld.java -d /tmp/test` will create `/tmp/test/com/examples/HelloWorld.class`.

# Design considerations
The compiler is split into three parts: [Lexer](src/main/java/io/github/martinschneider/orzo/lexer/Lexer.java), [Parser](src/main/java/io/github/martinschneider/orzo/parser/Parser.java) and [Code Generator](src/main/java/io/github/martinschneider/orzo/codegen/CodeGenerator.java).

It only uses Java core libraries and has no external dependencies (except for unit testing which requires [JUnit](https://junit.org).

To make the code less verbose, public fields and static imports are heavily used. This might not be considered best practice for other Java projects but it serves its purpose well for this one.

## Lexer
The Lexer uses a [`PushbackReader`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/io/PushbackReader.html) with a wrapper around it which keeps track of line information to facilitate useful error messages.

Its input is a [`File`](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/io/File.html), the output is a [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java).

### Parser

The Parser takes a [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) and produces a [`Clazz`](src/main/java/io/github/martinschneider/orzo/parser/prdocutions/Clazz.java) instance which represents the AST of the program.

A [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) has a pointer which keeps track of its current position.

There is (roughly) one implementation of the [`ProdParser`](src/main/java/io/github/martinschneider/orzo/parser/ProdParser.java) interface [for every production](src/main/java/io/github/martinschneider/orzo/parser) in the grammar. Its `parse` method will return a production if it matches and `null` otherwise. It must also set the pointer in the [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) accordingly:

> If there is a match, the token index must point to the next token after that match. If there is no match, the token index must be restored to the val before parse has been called.

This allows passing the [`TokenList`](src/main/java/io/github/martinschneider/orzo/lexer/TokenList.java) through the different sub-parsers.

We use a very simplistic dependency injection pattern to call different [`ProdParser`](src/main/java/io/github/martinschneider/orzo/parser/ProdParser.java) implementations from each other. Each class has an instance of [`ParserContext`](src/main/java/io/github/martinschneider/orzo/parser/ParserContext.java) which has references to everything else.

### Code generator

The Code generator takes the AST returned by the parser and outputs a byte array (wrapped inside an [`Output`](src/main/java/io/github/martinschneider/orzo/codegen/Output.java) object) which is then written to a JVM-compatible class file.

Similarly to the parser, there is one implementation of [`StatementGenerator`](src/main/java/io/github/martinschneider/orzo/codegen/statement/StatementGenerator.java) [for each statement type](src/main/java/io/github/martinschneider/orzo/codegen/statement) and a similar dependency injection concept (see [`CGContext`](src/main/java/io/github/martinschneider/orzo/codegen/CGContext.java)).

### Tests

High coverage with meaningful tests is crucial for a project like this. There are five types of tests used:

 - Lexer: [unit tests](src/test/java/io/github/martinschneider/orzo/lexer) for each token type
 - Parser: [unit tests](src/test/java/io/github/martinschneider/orzo/parser) for each production type
 - Code Generator: [unit tests](src/test/java/io/github/martinschneider/orzo/codegen) for each production/statement type (currently missing)
 - [integration tests](src/test/java/io/github/martinschneider/orzo/OrzoTest.java) compiling [sample programs](src/test/resources/io/github/martinschneider/orzo/tests) using Orzo and verifying their output against [predefined expectations](src/test/resources/io/github/martinschneider/orzo/tests/output)
 - self-compilation tests (WIP)