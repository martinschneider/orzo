# Kommpeiler

(Not yet) self-compiling Java compiler with some added syntactical sugar.

# Features
- [X] int, short, byte and long types
- [X] basic operations (+, -, *, /, %)
- [X] post-increment and decrement operators (++, --)
- [ ] pre-increment and decrement operators (++, --)
- [ ] assignment operators (+=, -=, *=, /=, %=)
- [ ] double and float types
- [X] System.out.println for String, int and long arguments
- [ ] String operations
- [ ] Arrays
- [ ] general types
- [X] boolean operations (==, !=, <, <=, >=, >)
- [ ] bit shift operations
- [X] if/else, while, do and for constructs
- [X] break statement
- [ ] switch statement
- [ ] lambda operator
- [X] static method calls (within the same class)
- [ ] imports

## Non-standard Java features
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b = b+1,a-1`
- [ ] uint, ushort, ubyte, ulong types
- [ ] 128 bit integer type

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
