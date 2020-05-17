# Kommpeiler

(Not yet) self-compiling Java compiler with some added syntactical sugar.

# Currently supporting
- System.out.println for String and int arguments
- int assignments and operations (+, -, *, /, %)
- boolean operations (==, !=, <, <=, >=, >)
- static method calls (within the same class)
- if, while, do and for constructs

## Non-standard Java features
- [parallel assignment](https://en.wikipedia.org/wiki/Assignment_(computer_science)#Parallel_assignment), e.g. `a,b = b+1,a-1`

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
