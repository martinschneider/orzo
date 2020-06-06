package io.github.martinschneider.orzo.examples;

public class QuickSort {
  public static void main(String[] args) {
    int[] array = new int[] {88, 34, 81, 78, 77, 55, 59, 14, -12, 54};
    qsort(array, 0, 9);
    printArray(array);
  }

  public static void qsort(int[] array, int start, int end) {
    if (start < end) {
      int index = partition(array, start, end);
      qsort(array, start, index - 1);
      qsort(array, index, end);
    }
  }

  public static void printArray(int[] array) {
    for (int i = 0; i < 10; i++) {
      System.out.println(array[i]);
    }
  }

  public static int partition(int[] array, int left, int right) {
    if (left != right) {
      int pivot = array[left];
      while (left <= right) {
        while (array[left] < pivot) {
          left++;
        }
        while (array[right] > pivot) {
          right--;
        }
        if (left <= right) {
          array[left], array[right] = array[right], array[left];
          left++;
          right--;
        }
      }
    }
    return left;
  }
}
