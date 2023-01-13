package io.github.martinschneider.orzo.lexer.tokens;

public enum TokenType {
	ID,
	INT_LITERAL,
	BOOL_LITERAL,
	LONG_LITERAL,
	FLOAT_LITERAL,
	DOUBLE_LITERAL,
	STRING_LITERAL,
	CHAR_LITERAL,
	TYPE,
	OP,
	KEYWORD,
	SCOPE,
	SYMBOL,
	EOF;
}
