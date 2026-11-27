package io.github.martinschneider.orzo.tests;

// several top-level classes in one file (and empty declarations)
public class TopLevelClasses {
  public static void main(String[] args) {
    Shape shape = new Square(3);
    System.out.println(shape.area() + " " + new Helper().greet());
  }
}

interface Shape {
  int area();
}

class Square implements Shape {
  int side;

  Square(int side) {
    this.side = side;
  }

  public int area() {
    return side * side;
  }
  ;
}

class Helper {
  String greet() {
    return "hi";
  }
}
;
