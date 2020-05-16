package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.List;
import java.util.stream.Collectors;

// extending Token is not the most elegant solution but it helps with parsing method calls as part
// of expressions
public class MethodCall extends Token implements Statement {
  private List<Identifier> names;
  private List<Expression> parameters;

  public MethodCall(final List<Identifier> names, final List<Expression> parameters) {
    super(names);
    this.names = names;
    this.parameters = parameters;
  }

  public List<Identifier> getNames() {
    return names;
  }

  public List<Expression> getParameters() {
    return parameters;
  }

  public String getQualifiedName() {
    return names.stream().map(x -> x.getValue().toString()).collect(Collectors.joining("."));
  }

  public void setName(final List<Identifier> names) {
    this.names = names;
  }

  public void setParameters(final List<Expression> parameters) {
    this.parameters = parameters;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(
        names.stream().map(x -> x.getValue().toString()).collect(Collectors.joining(".")));
    strBuilder.append('(');
    strBuilder.append(parameters.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
