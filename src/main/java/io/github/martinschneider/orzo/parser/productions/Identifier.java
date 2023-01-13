package io.github.martinschneider.orzo.parser.productions;

public class Identifier extends ExpressionToken{
	public String name;
	public Identifier next;
	public ArraySelector arrSel;
	
	public static Identifier of(String name)
	{
	  Identifier id = new Identifier();
      id.name = name;
      return id;
	}
}
