package io.github.martinschneider.orzo.tests;

public enum Status {
  ACTIVE(1),
  INACTIVE(0);
 
  public int id;

  private Status(int id) {
	this.id = id;
  }
}