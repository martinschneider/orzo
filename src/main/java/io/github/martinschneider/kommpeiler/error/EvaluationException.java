package io.github.martinschneider.kommpeiler.error;

public class EvaluationException extends Exception {
  private static final long serialVersionUID = -8004568316685052859L;

  public EvaluationException(final String error) {
    super(error);
  }
}
