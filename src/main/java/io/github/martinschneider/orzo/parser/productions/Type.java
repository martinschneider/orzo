package io.github.martinschneider.orzo.parser.productions;

import java.util.Objects;

public class Type {
	public String name;
	public byte arr; // 0 = no array, 1 = [], 2= [][] etc.

	public static Type of(String name) {
		Type type = new Type();
		type.name = name;
		return type;
	}

	public String descr() {
		StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < arr; i++) {
			strBuilder.append("[");
		}
		strBuilder.append(name);
		return strBuilder.toString();
	}

	@Override
	public String toString() {
		return "Type [name=" + name + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Type other = (Type) obj;
		return Objects.equals(name, other.name);
	}

}
