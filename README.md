# Kommpeiler

(Not yet) self-compiling Java compiler

# Currently supporting
- System.out.println for String and int arguments
- int assignments and operations (+, -, *, /, %)
- boolean operations (==, !=, <, <=, >=, >)
- static method calls (within the same class)
- if, while, do and for constructs
- parallel variable assignments

Working examples can be found [here](src/test/resources/io/github/martinschneider/kommpeiler/examples).

# Usage

To compile `Example.java` run:

`java -jar kommpeiler.java Example.java Example.class`
