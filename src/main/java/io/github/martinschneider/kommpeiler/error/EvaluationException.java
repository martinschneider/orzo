package io.github.martinschneider.kommpeiler.error;

/**
 * Evaluation Exception
 *
 * @author Martin Schneider
 */
public class EvaluationException extends Exception {
  private static final long serialVersionUID = -8004568316685052859L;

  /** @param error error-message */
  public EvaluationException(final String error) {
    super(error);
  }
}
