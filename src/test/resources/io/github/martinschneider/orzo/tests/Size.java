package io.github.martinschneider.orzo.tests;

public enum Size {
  SMALL("S"),
  MEDIUM("M"),
  LARGE("L");

  public String label;

  private Size(String label) {
    this.label = label;
  }
}