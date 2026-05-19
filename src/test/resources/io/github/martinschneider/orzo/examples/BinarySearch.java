package io.github.martinschneider.orzo.examples;

public class BinarySearch {
  public static void main(String[] args) {
    int[] arr = new int[] {2, 5, 8, 12, 16, 23, 38, 56, 72, 91};
    System.out.println(search(arr, 0, 9, 23));
    System.out.println(search(arr, 0, 9, 1));
    System.out.println(search(arr, 0, 9, 91));
    System.out.println(search(arr, 0, 9, 38));
  }

  public static int search(int[] arr, int lo, int hi, int target) {
    unless (lo <= hi) {
      return -1;
    }
    int mid = (lo + hi) / 2;
    if (arr[mid] == target) {
      return mid;
    }
    if (arr[mid] < target) {
      return search(arr, mid + 1, hi, target);
    }
    return search(arr, lo, mid - 1, target);
  }
}
