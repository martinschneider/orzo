# Kommpeiler

(Not yet) self-compiling Java compiler with some added syntactical sugar.

# Features

## Types
- [X] short
- [X] byte
- [X] int
- [X] long
- [X] float
- [X] double
- [ ] boolean
- [ ] arrays
- [X] String
- [ ] reference types

## Operators
- [X] assignment `=`
- [X] arithmetic `+`, `-`, `*`, `/`, `%`)
- [X] arithmetic expressions, e.g. `a + (3 * b) / c % 5`
- [X] post-increment and decrement `++`, `--`, e.g. `a++`
- [ ] pre-increment and decrement `++`, `--`, e.g. `++a`
- [ ] compound assignments `+=`, `-=`, `*=`, `/=`, `%=`
- [X] boolean `==`, `!=`, `<`, `<=`, `>=`, `>`
- [X] bit shifts `<<`, `>>`, `>>>`
- [ ] String concatenation

## Statements
- [X] System.out.println
- [X] if/else, while, do and for statements
- [X] break statement
- [ ] switch statement
- [ ] lambda operator
- [X] static method calls within the same class
- [ ] other method calls
- [ ] constructor calls

## Non-standard Java features
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b = b+1,a-1`
- [ ] uint, ushort, ubyte, ulong types

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
