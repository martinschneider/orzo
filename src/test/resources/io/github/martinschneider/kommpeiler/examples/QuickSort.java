package io.github.martinschneider.kommpeiler.examples;

public class QuickSort {
  public static void main(String[] args) {
    int[] a = new int[] {12, 423, 0, 76, 13, 23, 54, 48, 33, 10};
    for (int i = 0; i < 10; i++) {
      System.out.println(a[i]);
    }
    qsort(a, 0);
    for (int i = 0; i < 10; i++) {
      System.out.println(a[i]);
    }
  }

  public static void qsort(int[] array, int start, int end) {
    if (start < end) {
      int index = partition(array, start, end);
      qsort(array, start, index - 1);
      qsort(array, index, end);
    }
  }

  public static int partition(int[] array, int left, int right) {
    if (left != right) {
      int pivot = selectPivot(array, left, right);
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

  public static int selectPivot(int[] array, int left, int right) {
    return array[left];
  }
}
