package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.I2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2L;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;

public class BasicGenerator {
  private CGContext ctx;

  public BasicGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    if (from.equals(INT) && to.equals(DOUBLE)) {
      out.write(I2D);
      ctx.opStack.pop();
      ctx.opStack.push(DOUBLE);
    } else if (from.equals(INT) && to.equals(LONG)) {
      out.write(I2L);
      ctx.opStack.pop();
      ctx.opStack.push(LONG);
    }
    // TODO: others
  }
}
