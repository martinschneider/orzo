package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.List;
import java.util.Objects;

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
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(type.toString());
    for (int i = 0; i < arrDim; i++) {
      strBuilder.append("[]");
    }
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append('=');
    strBuilder.append(val);
    return strBuilder.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash(accFlags, arrDim, isField, name, type, val);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Declaration other = (Declaration) obj;
    return Objects.equals(accFlags, other.accFlags)
        && arrDim == other.arrDim
        && isField == other.isField
        && Objects.equals(name, other.name)
        && Objects.equals(type, other.type)
        && Objects.equals(val, other.val);
  }
}
