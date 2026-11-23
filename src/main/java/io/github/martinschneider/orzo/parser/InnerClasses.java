package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

/** The synthetic members of inner, local and anonymous classes (like javac's). */
public class InnerClasses {
  // the field of an inner class holding its enclosing instance
  public static final String OUTER_INSTANCE = "this$0";

  private InnerClasses() {}

  /**
   * An inner class gets its enclosing instance in a field "this$0", passed to its constructors as
   * their first argument (and set before the field initializers run).
   */
  public static void makeInner(Clazz inner, Clazz outer) {
    inner.isInner = true;
    addFields(inner, true, List.of(new Argument(outer.fqn(), new Identifier(OUTER_INSTANCE))));
  }

  /**
   * Adds fields set from new arguments of the constructors (the first or the last ones): set before
   * the field initializers run, passed on by "this(...)".
   */
  public static void addFields(Clazz clazz, boolean first, List<Argument> fields) {
    List<Statement> inits = new ArrayList<>();
    for (Argument arg : fields) {
      clazz.fields.add(field(arg));
      inits.add(storeField(arg.name.val.toString()));
    }
    inits.addAll(clazz.instanceInits);
    clazz.instanceInits = inits;
    for (Method method : clazz.methods) {
      if (!(method instanceof Constructor)) {
        continue;
      }
      List<Argument> args = new ArrayList<>(method.args);
      args.addAll(first ? 0 : args.size(), fields);
      method.args = args;
      if (!method.body.isEmpty()
          && method.body.get(0) instanceof MethodCall
          && ((MethodCall) method.body.get(0)).name.equals("this")) {
        // (the other constructor sets them)
        MethodCall thisCall = (MethodCall) method.body.get(0);
        List<Expression> params = new ArrayList<>(thisCall.params);
        List<Expression> passed = new ArrayList<>();
        for (Argument arg : fields) {
          passed.add(
              new Expression(new ArrayList<>(List.of(new Identifier(arg.name.val.toString())))));
        }
        params.addAll(first ? 0 : params.size(), passed);
        thisCall.params = params;
      }
    }
  }

  // a final field for an argument (an array type starts with "[")
  private static ParallelDeclaration field(Argument arg) {
    boolean isArray = arg.type.startsWith("[");
    Declaration field =
        new Declaration(
            List.of(AccessFlag.ACC_FINAL),
            isArray ? arg.type.substring(1) : arg.type,
            isArray ? 1 : 0,
            new Identifier(arg.name.val.toString()),
            null);
    field.isField = true;
    field.genericType = arg.genericType;
    return new ParallelDeclaration(new ArrayList<>(List.of(field)));
  }

  // "this.name = name;"
  private static Assignment storeField(String name) {
    Identifier target = new Identifier("this");
    target.next = new Identifier(name);
    return new Assignment(
        new ArrayList<>(List.of(target)),
        new ArrayList<>(List.of(new Expression(new ArrayList<>(List.of(new Identifier(name)))))));
  }
}
