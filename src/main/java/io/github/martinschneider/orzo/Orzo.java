package io.github.martinschneider.orzo;

import io.github.martinschneider.orzo.codegen.ClassChecker;
import io.github.martinschneider.orzo.codegen.CodeGenerator;
import io.github.martinschneider.orzo.codegen.Output;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.Parser;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// main entry point
public class Orzo {
  public static void main(String args[]) throws IOException {
    int verbose = 0;
    if (args.length == 0) {
      System.err.println("Syntax: orzo inputFiles -d outputPath [-v [level]]");
      System.exit(2);
    } else {
      List<File> inputs = new ArrayList<>();
      String outputPath = null;
      for (int i = 0; i < args.length; i++) {
        if (args[i].equals("-v")) {
          verbose = 1;
          if (args.length >= i + 2 && args[i + 1].matches("[0-9]+")) {
            verbose = Integer.parseInt(args[i + 1]);
            i++;
          }
        } else if (!args[i].equals("-d")) {
          inputs.add(new File(args[i]));
        } else {
          if (args.length < i + 2) {
            System.err.println("Missing argument for -d option");
            System.exit(2);
          }
          outputPath = args[i + 1];
          i++;
        }
      }
      if (!new Orzo(inputs, outputPath, verbose).compile()) {
        System.exit(1);
      }
    }
  }

  private List<File> inputs;
  private String outputPath;
  private int verbose;
  // package private for unit test
  List<Clazz> clazzes = new ArrayList<>();
  // the errors of the last compile()
  CompilerErrors errors;

  public Orzo(List<File> inputs, String outputPath, int verbose) {
    this.inputs = inputs;
    this.outputPath = outputPath;
    this.verbose = verbose;
  }

  public boolean compile() throws IOException {
    return compile(new ArrayList<>());
  }

  /**
   * Compiles the input files: the class files are written to the output path (or only into the
   * given outputs if it is null) unless there are errors, which are printed to stderr. Returns
   * whether the compilation succeeded.
   */
  public boolean compile(List<Output> outputs) throws IOException {
    clazzes = new ArrayList<>();
    errors = new CompilerErrors();
    List<Clazz> nestedClazzes = new ArrayList<>();
    List<Clazz> localClazzes = new ArrayList<>();
    for (int i = 0; i < inputs.size(); i++) {
      if (verbose > 0) {
        log("Reading from: " + inputs.get(i).getAbsolutePath());
      }
      errors.file = inputs.get(i).getName();
      TokenList tokens;
      try {
        tokens = new Lexer(errors).getTokens(inputs.get(i));
      } catch (IOException e) {
        errors.addError("read", "cannot read file: " + e.getMessage(), e.getStackTrace());
        continue;
      }
      if (verbose > 0) {
        log(
            "Scanner output: "
                + tokens.list().stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
      }
      Parser parser = new Parser(errors);
      errors.tokens = tokens;
      Clazz clazz;
      try {
        clazz = parser.parse(tokens);
      } catch (RuntimeException e) {
        // a bug in the parser: report it (at the current token) and go on with the next file
        errors.addError("parser", "internal compiler error: " + e, e.getStackTrace());
        continue;
      } finally {
        errors.tokens = null;
      }
      if (verbose > 0) {
        log("Parser output: " + clazz);
      }
      if (clazz == null) {
        errors.addError(
            "parse", "expected a class, interface or enum", new RuntimeException().getStackTrace());
        continue;
      }
      clazz.sourceFile = inputs.get(i).getName();
      clazzes.add(clazz);
      // (the test harness supplies one output per input file)
      if (outputs.size() <= i) {
        outputs.add(inMemoryOutput());
      }
      for (Clazz nested : parser.ctx.nestedClazzes) {
        nested.sourceFile = clazz.sourceFile;
        nestedClazzes.add(nested);
      }
      for (Clazz local : parser.ctx.localClazzes) {
        local.sourceFile = clazz.sourceFile;
        localClazzes.add(local);
      }
    }
    // no code generation for files with syntax errors (it would only report follow-up errors)
    if (!errors.isEmpty()) {
      System.err.print(errors.toString(verbose));
      return false;
    }
    // Static nested classes are compiled like the classes of separate input files. They come after
    // those (outputs stay index-aligned with the input files), as do classes discovered during
    // code generation (e.g. a lambda expression's synthetic implementation).
    nestedClazzes.addAll(localClazzes);
    for (Clazz nested : nestedClazzes) {
      clazzes.add(nested);
      outputs.add(inMemoryOutput());
    }
    new ClassChecker(errors, clazzes).check();
    if (!errors.isEmpty()) {
      System.err.print(errors.toString(verbose));
      return false;
    }
    CodeGenerator codeGen = new CodeGenerator(clazzes, outputs, errors);
    codeGen.generate();
    if (!errors.isEmpty()) {
      System.err.print(errors.toString(verbose));
      return false;
    }
    if (!errors.warnings.isEmpty()) {
      System.err.print(errors.toString(verbose));
    }
    // only now (without errors) write the class files
    if (outputPath != null) {
      for (int i = 0; i < clazzes.size(); i++) {
        File outputFile = new File(classPath(outputPath, clazzes.get(i)));
        if (verbose > 0) {
          log("Writing to: " + outputFile.getAbsolutePath());
        }
        if (!outputFile.getParentFile().exists()) {
          outputFile.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
          fos.write(outputs.get(i).getBytes());
        }
      }
    }
    return true;
  }

  // verbose output (-v) goes to stderr, like the errors (stdout is left to the program's users)
  private static void log(String message) {
    System.err.println(message);
  }

  // Output always buffers what's written to it (getBytes()); the PrintStream is irrelevant
  private static Output inMemoryOutput() {
    return new Output(new PrintStream(new ByteArrayOutputStream()));
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
    if (clazz.outerName != null) {
      strBuilder.append(clazz.outerName).append('$');
    }
    strBuilder.append(clazz.name);
    strBuilder.append(".class");
    return strBuilder.toString();
  }
}
