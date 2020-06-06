package io.github.martinschneider.orzo.tests;

public class NestedLoops {
  public static void main(String[] args) {
    for (int i = 1; i <= 3; i++) {
      for (int j = 3; j >= 1; j--) {
        for (int k = 1; k <= 3; k++) {
          for (int l = 3; l > 0; l--) {
            for (int m = 1; m <= 3; m++) {
              for (int n = 3; n >= 1; n--) {
                for (int o = 1; o <= 3; o++) {
                  if (i == j) {
                    if (j == k) {
                      if (k == l) {
                        if (l == m) {
                          if (m == n) {
                            if (n == o) {
                              System.out.println(i + j + k + l + m + n + o);
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
