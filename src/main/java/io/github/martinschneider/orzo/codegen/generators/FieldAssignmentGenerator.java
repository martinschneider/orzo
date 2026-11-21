package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.PUTFIELD;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.FieldProcessor;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.List;

public class FieldAssignmentGenerator implements StatementGenerator<FieldAssignment> {
  private static final String LOGGER_NAME = "field assignment generator";
  private CGContext ctx;

  public FieldAssignmentGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, FieldAssignment stmt) {
    // evaluate the object: the selector chain without its last link (the field)
    Identifier beforeField = stmt.target;
    while (beforeField.next.next != null) {
      beforeField = beforeField.next;
    }
    Identifier field = beforeField.next;
    beforeField.next = null;
    ExpressionResult object;
    try {
      object = ctx.exprGen.eval(out, null, new Expression(List.of(stmt.target)));
    } finally {
      beforeField.next = field;
    }
    String objectType = (object != null) ? object.type : null;
    FieldProcessor.InstanceField instanceField =
        (objectType == null)
            ? null
            : new FieldProcessor()
                .getInstanceFieldMap(objectType, ctx.allClazzes)
                .get(field.val.toString());
    if (instanceField == null) {
      ctx.errors.addError(
          LOGGER_NAME,
          String.format("unknown field %s of %s", field.val, objectType),
          new RuntimeException().getStackTrace());
      return out;
    }
    ExpressionResult value = ctx.exprGen.eval(out, instanceField.fieldType, stmt.value);
    ctx.basicGen.convert1(out, value.type, instanceField.fieldType);
    String owner = instanceField.className.replace('.', '/');
    String descr = TypeUtils.descr(instanceField.fieldType);
    ctx.constPool.addClass(owner);
    ctx.constPool.addFieldRef(owner, instanceField.fieldName, descr);
    out.write(PUTFIELD);
    out.write(ctx.constPool.indexOf(CONSTANT_FIELDREF, owner, instanceField.fieldName, descr));
    ctx.opStack.pop2();
    return out;
  }
}
