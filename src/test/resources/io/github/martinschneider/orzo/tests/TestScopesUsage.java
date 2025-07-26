package io.github.martinschneider.orzo.tests;

public class TestScopesUsage {
  public static void main(String[] args) {
    System.out.println(TestScopes.PUBLIC.accFlag.val);  // Should print 1
    System.out.println(TestScopes.PRIVATE.accFlag.val); // Should print 2
  }
}