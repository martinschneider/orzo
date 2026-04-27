package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class Declaration {
  public List<AccessFlag> accFlags;
  public boolean isField;
  public Identifier name;
  public int arrDim;
  public String type;
  public Expression val;

  public Declaration(
      List<AccessFlag> accFlags, String type, int arrDim, Identifier name, Expression val) {
    this.accFlags = accFlags;
    this.type = type;
    this.arrDim = arrDim;
    this.name = name;
    this.val = val;
  }

  public Declaration(List<AccessFlag> accFlags, String type, Identifier name, Expression val) {
    this(accFlags, type, 0, name, val);
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
