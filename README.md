# Kommpeiler

(Not yet) self-compiling Java compiler with some added syntactical sugar.

# Features
- [X] int assignments and operations (+, -, *, /, %)
- [ ] short, byte, float, double and boolean types
- [X] System.out.println for String and int arguments
- [ ] String operations
- [ ] arrays
- [ ] general types
- [X] boolean operations (==, !=, <, <=, >=, >)
- [X] bit shift operations
- [X] if/else, while, do and for constructs
- [ ] switch statement
- [ ] lambda operator
- [X] static method calls (within the same class)
- [ ] imports

## Non-standard Java features
- [X] [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b = b+1,a-1`

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
