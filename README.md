# Kommpeiler

(Not yet) self-compiling Java compiler with some added syntactical sugar.

# Features
- [X] short, byte, int and long types
- [X] float and double types
- [ ] boolean type
- [X] arithmetic operators (`+`, `-`, `*`, `/`, `%`)
- [X] arithmetic expressions, e.g. `(a + (3 * b) / c % 5)`
- [X] post-increment and decrement operators (`++`, `--`)
- [ ] pre-increment and decrement operators (`++`, `--`)
- [ ] assignment operators (`=`, `+=`, `-=`, `*=`, `/=`, `%=`)
- [X] System.out.println support
- [ ] String operations
- [ ] Arrays
- [ ] general types
- [X] boolean operators (`==`, `!=`, `<`, `<=`, `>=`, `>`)
- [ ] bit shift operators (`<<`, `<<<`, `>>`, `>>>`)
- [X] if/else, while, do and for statements
- [X] break statement
- [ ] switch statement
- [ ] lambda operator
- [X] static method calls within the same class
- [ ] general method calls
- [ ] constructors
- [ ] imports

## Non-standard Java features
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b = b+1,a-1`
- [ ] uint, ushort, ubyte, ulong types
- [ ] 128 bit integer type
- [ ] defer

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
