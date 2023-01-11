package io.github.martinschneider.orzo.lexer.tokens;

import java.util.Objects;

public class Token {
	public String type;
	public String val;
	public Location loc;

	public static Token of(String type, String val) {
		Token t = new Token();
		t.type = type;
		t.val = val;
		return t;
	}

	public Token wLoc(Location loc) {
		this.loc = loc;
		return this;
	}

	public static Token chr(char val) {
		return Token.of("chr", Character.toString(val));
	}

	public static Token str(String val) {
		return Token.of("str", val);
	}

	public static Token int32(String val) {
		return Token.of("int32", val);
	}

	public static Token int64(String val) {
		return Token.of("int64", val);
	}

	public static Token float32(String val) {
		return Token.of("float32", val);
	}

	public static Token float64(String val) {
		return Token.of("float64", val);
	}

	public static Token id(String val) {
		return Token.of("id", val);
	}

	public static Token op(Operators val) {
		return Token.of("op", val.name());
	}

	public static Token keyword(Keywords val) {
		return Token.of("keyword", val.name());
	}

	public static Token scope(Scopes val) {
		return Token.of("scope", val.name());
	}

	@Override
	public String toString() {
		return "Token [type=" + type + ", val=" + val + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, val);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Token other = (Token) obj;
		return Objects.equals(type, other.type) && Objects.equals(val, other.val);
	}

	public static Token sym(Symbols val) {
		return Token.of("sym", val.name());
	}

	public static Token type(String val) {
		return Token.of("type", val);
	}

	public static Token bool(String val) {
		return Token.of("id", val);
	}

	public static Token eof() {
		return Token.of("eof", null);
	}

	public boolean eq(Token val) {
		return equals(val);
	}
}
