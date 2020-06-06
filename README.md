![Orzo](logo.png)

A (not yet) self-compiling Java compiler with some added syntactical sugar.

# Features

## Types
- [X] short
- [X] byte
- [X] int
- [X] long
- [X] float
- [X] double
- [ ] boolean
- [X] one-dimensional arrays
- [ ] multi-dimensional arrays
- [X] String
- [ ] reference types
- [ ] class inheritance
- [ ] interfaces
- [ ] enums

## Operators
- [X] assignment `=`
- [X] arithmetic `+`, `-`, `*`, `/`, `%`
- [X] parenthesis `(` ,`)`
- [X] unary post-increment and decrement `++`, `--`
- [ ] unary pre-increment and decrement `++`, `--`
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
- [ ] String concatenation `+`
- [ ] ternary `?:`
- [ ] cast `()`
- [ ] object creation `new`

## Statements
- [X] System.out.println
- [X] if/else if/else
- [X] while loops
- [X] do loops
- [X] for loops
- [X] break
- [ ] switch
- [ ] lambdas
- [X] static method calls within the same class
- [ ] other method calls
- [ ] constructor calls

## Non-standard Java features
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b=b+1,a-1`
- [ ] uint, ushort, ubyte, ulong types

## Notes
- Array defintions must be of the form `int[] a`, `int a[]` is not supported (because it's confusing and ugly) 

# Examples
Working examples can be found [here](src/test/resources/io/github/martinschneider/orzo/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
