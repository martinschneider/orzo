package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MethodCall
 *
 * @author Martin Schneider
 */
public class MethodCall extends Statement {
  private List<Identifier> names;
  private List<Factor> parameters;

  /**
   * @param name name
   * @param parameters list of parameters
   */
  public MethodCall(final List<Identifier> names, final List<Factor> parameters) {
    super();
    this.names = names;
    this.parameters = parameters;
  }

  public List<Identifier> getNames() {
    return names;
  }

  public List<Factor> getParameters() {
    return parameters;
  }

  public String getQualifiedName() {
    if (names.isEmpty()) {
      return "";
    } else {
      StringBuilder strBuilder = new StringBuilder(names.get(0).getValue());
      for (int i = 1; i < names.size(); i++) {
        strBuilder.append('.');
        strBuilder.append(names.get(i).getValue());
      }
      return strBuilder.toString();
    }
  }

  public void setName(final List<Identifier> names) {
    this.names = names;
  }

  public void setParameters(final List<Factor> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(names.stream().map(x -> x.getValue()).collect(Collectors.joining(".")));
    strBuilder.append('(');
    strBuilder.append(
        parameters.stream().map(x -> x.getValue().toString()).collect(Collectors.joining(", ")));
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
