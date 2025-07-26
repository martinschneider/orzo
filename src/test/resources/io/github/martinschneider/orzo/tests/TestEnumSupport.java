package io.github.martinschneider.orzo.tests;

public class TestEnumSupport {
  public static void main(String[] args) {
    // Test AccessFlag enum with short constructor parameters and hex literals
    System.out.println(AccessFlagTest.ACC_PUBLIC.val);  // Should print 1 (0x0001)
    System.out.println(AccessFlagTest.ACC_PRIVATE.val); // Should print 2 (0x0002)
    
    // Test existing Status enum with int constructor parameters
    System.out.println(Status.ACTIVE.id);   // Should print 1
    System.out.println(Status.INACTIVE.id); // Should print 0
  }
}