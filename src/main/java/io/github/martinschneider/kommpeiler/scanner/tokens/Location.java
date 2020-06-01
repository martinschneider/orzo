package io.github.martinschneider.kommpeiler.scanner.tokens;

import io.github.martinschneider.kommpeiler.util.Pair;

public class Location {
  public Location(Pair<Integer, Integer> coord) {
    super();
    this.coord = coord;
  }

  private Pair<Integer, Integer> coord;

  public Pair<Integer, Integer> getCoord() {
    return coord;
  }

  public void setCoord(Pair<Integer, Integer> coord) {
    this.coord = coord;
  }

  public static Location of(int left, int right) {
    return new Location(new Pair<>(left, right));
  }

  @Override
  public String toString() {
    return "L" + coord.getLeft() + ":" + coord.getRight();
  }
}
