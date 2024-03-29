package io.github.martinschneider.orzo;

import io.github.martinschneider.orzo.codegen.CodeGenerator;
import io.github.martinschneider.orzo.codegen.Output;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.Parser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// main entry point
public class Orzo {
  private static Output fileOutput(File outputFile) {
    if (!outputFile.getParentFile().exists()) {
      outputFile.getParentFile().mkdirs();
    }
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
    int verbose = 0;
    if (args.length == 0) {
      System.err.println("Syntax: orzo inputFiles -d outputPath");
    } else {
      List<File> inputs = new ArrayList<>();
      String outputPath = null;
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-v")) {
          if (args.length >= i + 2) {
            verbose = Integer.parseInt(args[i + 1]);
            i++;
          } else {
            verbose = 1;
          }
        } else if (!args[i].equals("-d")) {
          inputs.add(new File(args[i]));
        } else {
          if (args.length < i + 2) {
            System.err.println("Missing argument for -d option");
            return;
          }
          outputPath = args[i + 1];
          i++;
        }
      }
      new Orzo(inputs, outputPath, verbose).compile();
    }
  }

  private List<File> inputs;
  private String outputPath;
  private int verbose;
  // package private for unit test
  List<Clazz> clazzes = new ArrayList<>();

  public Orzo(List<File> inputs, String outputPath, int verbose) {
    this.inputs = inputs;
    this.outputPath = outputPath;
    this.verbose = verbose;
  }

  public void compile() throws IOException {
    compile(new ArrayList<>());
  }

  public void compile(List<Output> outputs) throws IOException {
    clazzes = new ArrayList<>();
    ParserContext ctx = null;
    for (int i = 0; i < inputs.size(); i++) {
      if (verbose > 0) {
        System.out.println("Reading from: " + inputs.get(i).getAbsolutePath());
      }
      Lexer scanner = new Lexer();
      TokenList tokens = scanner.getTokens(inputs.get(i));
      if (verbose > 0) {
        System.out.println(
            "Scanner output: "
                + tokens.list().stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
      }
      Parser parser = new Parser(scanner.getErrors());
      Clazz clazz = parser.parse(tokens);
      if (verbose > 0) {
        System.out.println("Parser output: " + clazz);
      }
      Output output = null;
      if (outputs != null && outputs.size() > i) {
        output = outputs.get(i);
      }
      if (output == null) {
        File outputFile = new File(classPath(outputPath, clazz));
        if (verbose > 0) {
          System.out.println("Writing to: " + outputFile.getAbsolutePath());
        }
        output = fileOutput(outputFile);
      }
      clazz.sourceFile = inputs.get(i).getName();
      clazzes.add(clazz);
      outputs.add(output);
      ctx = parser.ctx;
    }
    CodeGenerator codeGen = new CodeGenerator(clazzes, outputs, ctx.errors);
    codeGen.generate();
    if (!codeGen.getErrors().errors.isEmpty()) {
      System.err.println(String.format("%s", codeGen.getErrors().toString(verbose)));
    }
  }

  private String classPath(String dir, Clazz clazz) {
    if (dir == null) {
      dir = System.getProperty("user.dir");
    }
    StringBuilder strBuilder = new StringBuilder(dir);
    strBuilder.append(File.separator);
    if (clazz.packageName != null) {
      strBuilder.append(clazz.packageName.replaceAll("\\.", File.separator));
    }
    strBuilder.append(File.separator);
    strBuilder.append(clazz.name);
    strBuilder.append(".class");
    return strBuilder.toString();
  }
}
