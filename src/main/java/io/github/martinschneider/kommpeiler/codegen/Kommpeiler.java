package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.parser.Parser;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.scanner.Scanner;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class Kommpeiler {
  private static Output createOutput(File outputFile) {
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
    File input = new File(args[0]);
    File output = new File(args[1]);
    System.out.println(
        "╦╔═╔═╗╔╦╗╔╦╗╔═╗╔═╗╦╦  ╔═╗╦═╗\n"
            + "╠╩╗║ ║║║║║║║╠═╝║╣ ║║  ║╣ ╠╦╝\n"
            + "╩ ╩╚═╝╩ ╩╩ ╩╩  ╚═╝╩╩═╝╚═╝╩╚═");
    System.out.println("Reading from: " + input.getAbsolutePath());
    Scanner scanner = new Scanner();
    List<Token> tokens = scanner.getTokens(input);
    System.out.println("Scanner output: " + tokens);
    Parser parser = new Parser(tokens);
    Clazz clazz = parser.parseClass();
    System.out.println("Parser output: " + clazz);
    System.out.println("Writing to: " + output.getAbsolutePath());
    CodeGenerator codeGen = new CodeGenerator(clazz, createOutput(output));
    System.out.println("Ok bye!\n");
    codeGen.generate();
  }
}
