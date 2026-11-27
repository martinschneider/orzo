package io.github.martinschneider.orzo.tests;

// text blocks: incidental indentation, trailing spaces and escape sequences
public class TextBlocks {
  public static void main(String[] args) {
    String a = """
        Hello,
          World!
        """;
    String b = """
        one \
        line\s
        tab\there "quoted" \"""
        end""";
    String c = """
           x
         y
        """;
    String d = """
        trailing    
        A ok
          """;
    System.out.print(a);
    System.out.println("[" + b + "]");
    System.out.print(c);
    System.out.print(d);
    System.out.println(a.length() + " " + b.length() + " " + d.length());
  }
}
