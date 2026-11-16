package io.github.martinschneider.orzo.tests;

public class SuperFieldShadowing extends ParentFieldShadow {
  public void scan() {
    char character;
    character = 'L';
    System.out.println("local=" + character);
    System.out.println("super=" + super.character);
  }

  public static void main(String[] args) {
    new SuperFieldShadowing().scan();
  }
}
