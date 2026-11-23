package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.parser.InnerClasses.OUTER_INSTANCE;

import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.util.ArrayList;
import java.util.List;

/** The enclosing instances of inner classes (see InnerClasses.makeInner()). */
public class EnclosingInstances {
  private EnclosingInstances() {}

  /**
   * The fields ("this$0") to follow from an instance of a class to one of an enclosing class, as
   * constant pool indexes; null if there is no such instance (e.g. the class is static).
   */
  public static List<Integer> path(CGContext ctx, Clazz from, Clazz target) {
    List<Integer> path = new ArrayList<>();
    Clazz clazz = from;
    while (clazz != target) {
      Clazz outer = (clazz != null && clazz.isInner) ? outerOf(clazz, ctx.allClazzes) : null;
      if (outer == null) {
        return null;
      }
      String owner = clazz.fqn('/');
      String descr = "L" + outer.fqn('/') + ";";
      ctx.constPool.addFieldRef(owner, OUTER_INSTANCE, descr);
      path.add((int) ctx.constPool.indexOf(CONSTANT_FIELDREF, owner, OUTER_INSTANCE, descr));
      clazz = outer;
    }
    return path;
  }

  /** The class immediately enclosing a nested class (or null). */
  public static Clazz outerOf(Clazz clazz, List<Clazz> clazzes) {
    List<Clazz> outers = FieldProcessor.enclosingClasses(clazz, clazzes);
    return outers.isEmpty() ? null : outers.get(outers.size() - 1);
  }
}
