package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.util.Pair;

public class Location {

	public Pair coord;

	private Location(Pair coord) {
		super();
		this.coord = coord;
	}

	public static Location of(int left, int right) {
		return new Location(new Pair(left, right));
	}

	@Override
	public String toString() {
		return "L" + coord._left + ":" + coord._right;
	}
}
