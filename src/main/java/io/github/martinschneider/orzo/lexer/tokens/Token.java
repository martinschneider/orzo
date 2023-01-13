package io.github.martinschneider.orzo.lexer.tokens;

import static io.github.martinschneider.orzo.lexer.tokens.TokenType.BOOL_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.CHAR_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.DOUBLE_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.EOF;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.FLOAT_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.ID;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.INT_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.KEYWORD;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.LONG_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.OP;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.SCOPE;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.STRING_LITERAL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.SYMBOL;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.TYPE;

import java.util.List;
import java.util.Objects;

import io.github.martinschneider.orzo.parser.productions.ExpressionToken;
import io.github.martinschneider.orzo.parser.productions.Type;

public class Token {
	public TokenType type;
	public String val;
	public Location loc;

	public static Token of(TokenType type, String val) {
		Token t = new Token();
		t.type = type;
		t.val = val;
		return t;
	}

	public Token wLoc(Location loc) {
		this.loc = loc;
		return this;
	}

	public static Token chrLit(char val) {
		return Token.of(CHAR_LITERAL, Character.toString(val));
	}

	public static Token strLit(String val) {
		return Token.of(STRING_LITERAL, val);
	}

	public static Token intLit(String val) {
		return Token.of(INT_LITERAL, val);
	}

	public static Token longLit(String val) {
		return Token.of(LONG_LITERAL, val);
	}

	public static Token floatLit(String val) {
		return Token.of(FLOAT_LITERAL, val);
	}

	public static Token doubleLit(String val) {
		return Token.of(DOUBLE_LITERAL, val);
	}

	public static Token boolLit(String val) {
		return Token.of(BOOL_LITERAL, val);
	}

	public static Token id(String val) {
		return Token.of(ID, val);
	}

	public static Token op(Operator val) {
		return Token.of(OP, val.name());
	}

	public static Token keyword(Keyword val) {
		return Token.of(KEYWORD, val.name());
	}

	public static Token scope(Scope val) {
		return Token.of(SCOPE, val.name());
	}

	public static Token sym(Symbol val) {
		return Token.of(SYMBOL, val.name());
	}

	public static Token type(String val) {
		return Token.of(TYPE, val);
	}

	public static Token eof() {
		return Token.of(EOF, null);
	}

	public boolean eq(Token val) {
		return equals(val);
	}

	public boolean isType() {
		return type.equals(TYPE);
	}

	public boolean isLit() {
		return List.of(INT_LITERAL, BOOL_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL, STRING_LITERAL,
				CHAR_LITERAL).contains(type);
	}

	public boolean isIntLit() {
		return type.equals(INT_LITERAL);
	}

	public boolean isLongLit() {
		return type.equals(LONG_LITERAL);
	}

	public boolean isFloatLit() {
		return type.equals(FLOAT_LITERAL);
	}

	public boolean isDoubleLit() {
		return type.equals(DOUBLE_LITERAL);
	}

	public boolean isId() {
		return type.equals(ID);
	}

	public boolean isOp() {
		return type.equals(OP);
	}
	
	public boolean isScope() {
		return type.equals(SCOPE);
	}
	
	public boolean isEOF()
	{
		return type.equals(EOF);
	}
	
	public boolean isNum() {
		return List.of(INT_LITERAL, LONG_LITERAL, FLOAT_LITERAL, DOUBLE_LITERAL).contains(type);
	}
	
	public void changeSign() {
		if (isNum())
		{
			val = "-" + val;
		}
		else
		{
			throw new UnsupportedOperationException("Token " + this + " is not a number.");
		}
	}
	
	public boolean eq(TokenType type, Object val)
	{
		return this.type.equals(type) && this.val.equals(val.toString());
	}

	public int intVal() {
		return Integer.parseInt(val);
	}

	public long longVal() {
		return Long.parseLong(val);
	}

	public float floatVal() {
		return Float.parseFloat(val);
	}

	public double doubleVal() {
		return Double.parseDouble(val);
	}
	
	public Operator opVal()
	{
		return Operator.valueOf(val);
	}
	
	public Scope scopeVal()
	{
		return Scope.valueOf(val);
	}
	
	public BasicType typeVal()
	{
		return BasicType.valueOf(val);
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
}
