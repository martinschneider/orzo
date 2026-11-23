package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class InnerClasses {
  // inner (non-static member) classes: they have an enclosing instance
  int count = 0;
  String name = "outer";
  List<String> log = new ArrayList<>();

  String greet(String who) {
    return "hello " + who + " from " + name;
  }

  class Counter {
    int step;

    Counter(int step) {
      this.step = step;
    }

    Counter() {
      this(1);
    }

    void tick() {
      count += step;
      count++;
      log.add("tick " + count);
    }

    String hello() {
      return greet("counter");
    }

    Counter twin() {
      return new Counter(step);
    }

    class Detail {
      String describe() {
        return name + " " + step + " " + count;
      }

      // (an enclosing instance by its class name)
      String qualified() {
        return InnerClasses.this.name + " " + Counter.this.step + " " + InnerClasses.this.greet("x");
      }
    }

    Detail detail() {
      return new Detail();
    }
  }

  void run() {
    Counter counter = new Counter(2);
    counter.tick();
    new Counter().tick();
    counter.twin().tick();
    System.out.println(count + " " + log);
    System.out.println(counter.hello());
    System.out.println(counter.detail().describe());
    System.out.println(counter.detail().qualified());
  }

  public static void main(String[] args) {
    new InnerClasses().run();
  }
}
