package io.github.martinschneider.orzo.lexer.tokens;

public enum BasicType {
	BYTE,
	SHORT,
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	CHAR,
	VOID,
	STRING; // TODO: String is not a basic type
	
	public static boolean isBasicType(String strType)
	{
		for (BasicType type : BasicType.values())
		{
			if (strType.toLowerCase().equals(type.name().toLowerCase()))
			{
				return true;
			}
		}
		return false;
	}
}
