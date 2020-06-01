package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.lexer.Lexer;
import io.github.martinschneider.kommpeiler.lexer.TokenList;
import io.github.martinschneider.kommpeiler.parser.Parser;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    System.out.println("\nReading from: " + input.getAbsolutePath());
    Lexer scanner = new Lexer();
    TokenList tokens = scanner.getTokens(input);
    System.out.println(
        "Scanner output: "
            + tokens.list().stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    Parser parser = new Parser(scanner.getErrors());
    Clazz clazz = parser.parse(tokens);
    System.out.println("Parser output: " + clazz);
    if (outputPath != null) {
      System.out.println("Writing to: " + outputPath);
    }
    CodeGenerator codeGen = new CodeGenerator(clazz, output, parser.ctx);
    codeGen.generate();
    if (!codeGen.getErrors().getErrors().isEmpty()) {
      StringBuilder errors = new StringBuilder("\n");
      int errCount = codeGen.getErrors().getErrors().size();
      errors.append(errCount);
      errors.append(" error");
      if (errCount > 1) {
        errors.append("s");
      }
      errors.append("\n");
      errors.append(codeGen.getErrors());
      System.out.println(errors.toString());
    }
    System.out.println("Ok bye!\n");
  }
}
