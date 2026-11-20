package io.github.martinschneider.orzo.tests;

public class VarargsHolder {
  public int count;

  public VarargsHolder(String... names) {
    count = names.length;
  }
}
