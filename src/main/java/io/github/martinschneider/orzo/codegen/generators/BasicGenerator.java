package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.castOps;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import java.util.Collections;

public class BasicGenerator {
  private CGContext ctx;

  public BasicGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    out.write(castOps.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]));
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }
}
