package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.parser.Parser;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.scanner.Lexer;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

// main entry point
public class Kommpeiler {
  private static Output fileOutput(File outputFile) {
    PrintStream fileOutput;
    Output out = null;
    try {
      fileOutput = new PrintStream(new FileOutputStream(outputFile));
      out = new Output(fileOutput);
      return out;
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String args[]) throws IOException {
    new Kommpeiler(new File(args[0]), fileOutput(new File(args[1])), args[1]).compile();
  }

  private File input;
  private Output output;
  private String outputPath;

  public Kommpeiler(File input, Output output, String outputPath) {
    this.input = input;
    this.output = output;
    this.outputPath = outputPath;
  }

  public void compile() throws IOException {
    System.out.println(
        "╦╔═╔═╗╔╦╗╔╦╗╔═╗╔═╗╦╦  ╔═╗╦═╗\n"
            + "╠╩╗║ ║║║║║║║╠═╝║╣ ║║  ║╣ ╠╦╝\n"
            + "╩ ╩╚═╝╩ ╩╩ ╩╩  ╚═╝╩╩═╝╚═╝╩╚═");
    System.out.println("Reading from: " + input.getAbsolutePath());
    Lexer scanner = new Lexer();
    List<Token> tokens = scanner.getTokens(input);
    System.out.println(
        "Scanner output: "
            + tokens.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    Parser parser = new Parser(tokens);
    Clazz clazz = parser.parseClass();
    System.out.println("Parser output: " + clazz);
    if (outputPath != null) {
      System.out.println("Writing to: " + outputPath);
    }
    CodeGenerator codeGen = new CodeGenerator(clazz, output);
    codeGen.generate();
    System.out.println("Ok bye!\n");
  }
}
