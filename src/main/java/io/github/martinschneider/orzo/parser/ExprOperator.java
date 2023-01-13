package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.parser.productions.ExpressionToken;

public class ExprOperator extends ExpressionToken{
	io.github.martinschneider.orzo.lexer.tokens.Operator op;
	
	public static ExprOperator of(io.github.martinschneider.orzo.lexer.tokens.Operator op)
	{
		ExprOperator exprOp = new ExprOperator();
		exprOp.op = op;
		return exprOp;
	}
}
