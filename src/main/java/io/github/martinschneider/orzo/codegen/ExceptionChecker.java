package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checked exceptions (JLS 11.2): one that a call or a throw statement may throw must be caught by
 * an enclosing catch block or declared by the method ("throws"), and a catch block must not catch a
 * checked exception its try block can't throw.
 */
public class ExceptionChecker {
  private static final String LOG_NAME = "exception checker";
  private final CGContext ctx;
  // the try blocks being generated, the innermost first
  private final Deque<TryBlock> tryBlocks = new ArrayDeque<>();
  private final Set<String> reported = new HashSet<>();

  /** The exceptions a try block catches, and the checked exceptions thrown in it. */
  public static class TryBlock {
    final List<String> caught;
    final Set<String> thrown = new HashSet<>();

    TryBlock(List<String> caught) {
      this.caught = caught;
    }
  }

  public ExceptionChecker(CGContext ctx) {
    this.ctx = ctx;
  }

  /** (for each method) */
  public void reset() {
    tryBlocks.clear();
  }

  public void enterTry(List<String> caught) {
    List<String> types = new ArrayList<>();
    for (String type : caught) {
      types.add(name(type));
    }
    tryBlocks.push(new TryBlock(types));
  }

  /** The end of a try block: its catch blocks must catch exceptions it can throw. */
  public void exitTry() {
    TryBlock tryBlock = tryBlocks.pop();
    for (String caught : tryBlock.caught) {
      if (!isChecked(caught)
          || caught.equals("java.lang.Exception")
          || caught.equals("java.lang.Throwable")) {
        continue;
      }
      boolean thrown = false;
      for (String type : tryBlock.thrown) {
        thrown |= isSubtype(type, caught) || isSubtype(caught, type);
      }
      if (!thrown) {
        report(
            "exception "
                + simpleName(caught)
                + " is never thrown in body of corresponding try statement");
      }
    }
  }

  /** The exceptions a called method or constructor declares. */
  public void thrown(Method method) {
    for (String exception : method.exceptions) {
      thrown(exception, true);
    }
  }

  /**
   * An exception that may be thrown (report: false for rethrowing a caught exception, which may
   * only throw what its try block throws).
   */
  public void thrown(String type, boolean report) {
    type = name(type);
    if (type == null || !isChecked(type)) {
      return;
    }
    for (TryBlock tryBlock : tryBlocks) {
      tryBlock.thrown.add(type);
      for (String caught : tryBlock.caught) {
        if (isSubtype(type, caught)) {
          return;
        }
      }
    }
    if (ctx.method != null) {
      for (String declared : ctx.method.exceptions) {
        if (isSubtype(type, declared)) {
          return;
        }
      }
    }
    if (report) {
      report(
          "unreported exception " + simpleName(type) + "; must be caught or declared to be thrown");
    }
  }

  private void report(String message) {
    // (a finally block is generated more than once)
    if (reported.add(System.identityHashCode(ctx.errors.position) + message)) {
      ctx.errors.addError(LOG_NAME, message, new RuntimeException().getStackTrace());
    }
  }

  private boolean isChecked(String type) {
    return isChecked(type, ctx.allClazzes);
  }

  private boolean isSubtype(String type, String of) {
    return isSubtype(type, of, ctx.allClazzes);
  }

  /** A subclass of Throwable, but not of RuntimeException or Error. */
  public static boolean isChecked(String type, List<Clazz> clazzes) {
    List<String> supertypes = superclasses(type, clazzes);
    return supertypes.contains("java.lang.Throwable")
        && !supertypes.contains("java.lang.RuntimeException")
        && !supertypes.contains("java.lang.Error");
  }

  public static boolean isSubtype(String type, String of, List<Clazz> clazzes) {
    return superclasses(type, clazzes).contains(name(of));
  }

  // the class and its superclasses (of this compilation run or loaded by reflection)
  private static List<String> superclasses(String type, List<Clazz> clazzes) {
    List<String> supertypes = new ArrayList<>();
    String name = name(type);
    while (name != null && !supertypes.contains(name)) {
      supertypes.add(name);
      Clazz source = TypeUtils.sourceClazz(name, clazzes);
      if (source != null) {
        name = name(source.baseClass);
        continue;
      }
      try {
        for (Class<?> c = Class.forName(name).getSuperclass(); c != null; c = c.getSuperclass()) {
          supertypes.add(c.getName());
        }
      } catch (ClassNotFoundException | LinkageError e) {
        // unknown
      }
      break;
    }
    return supertypes;
  }

  // e.g. "java.io.IOException" for "java/io/IOException" or "Ljava/io/IOException;"
  private static String name(String type) {
    if (type == null) {
      return null;
    } else if (type.startsWith("L") && type.endsWith(";")) {
      type = type.substring(1, type.length() - 1);
    }
    return type.replace('/', '.');
  }

  static String simpleName(String type) {
    return type.substring(type.lastIndexOf('.') + 1).replace('$', '.');
  }
}
