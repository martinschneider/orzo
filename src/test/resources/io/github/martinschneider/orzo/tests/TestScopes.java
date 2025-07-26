package io.github.martinschneider.orzo.tests;

// Simple test without static imports first
public enum TestScopes {
  PUBLIC(AccessFlagTest.ACC_PUBLIC),
  PRIVATE(AccessFlagTest.ACC_PRIVATE);

  public AccessFlagTest accFlag;

  private TestScopes(AccessFlagTest accFlag) {
    this.accFlag = accFlag;
  }
}