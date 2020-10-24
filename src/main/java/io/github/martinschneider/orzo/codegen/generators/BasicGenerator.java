package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARRAYLENGTH;
import static io.github.martinschneider.orzo.codegen.OpCodes.WIDE;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.castOps;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.castOps1;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import java.util.Collections;

public class BasicGenerator {
  private CGContext ctx;

  public BasicGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    // TODO: array casts
    if (from != null && to != null) {
      out.write(castOps.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]));
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  public void convert1(DynamicByteArray out, String from, String to) {
    // TODO: array casts
    if (from != null && to != null) {
      out.write(castOps1.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]));
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  public void wide(HasOutput out, short idx, byte opCode) {
    if (idx > Byte.MAX_VALUE) {
      out.write(WIDE);
      out.write(opCode);
      out.write(idx);
    } else {
      out.write(opCode);
      out.write((byte) idx);
    }
  }

  public void arrayLength(HasOutput out) {
    out.write(ARRAYLENGTH);
  }
}
