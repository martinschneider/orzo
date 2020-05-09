package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

/**
 * Assignment
 *
 * @author Martin Schneider
 */
public class Assignment extends Statement {

  private Identifier left;
  private Factor right;

  /** empty constructor */
  public Assignment() {}

  /**
   * @param left left side of assignment
   * @param right right side of assignment
   */
  public Assignment(final Identifier left, final Factor right) {
    this.setLeft(left);
    this.setRight(right);
  }

  public void setLeft(final Identifier left) {
    this.left = left;
  }

  public Identifier getLeft() {
    return left;
  }

  public void setRight(final Factor right) {
    this.right = right;
  }

  public Factor getRight() {
    return right;
  }

  /** {@inheritDoc} * */
  @Override
  public String toString() {
    return left.toString() + ":=" + right.toString();
  }
}
