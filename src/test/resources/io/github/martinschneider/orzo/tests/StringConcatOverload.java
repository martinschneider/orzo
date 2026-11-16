package io.github.martinschneider.orzo.tests;

public class StringConcatOverload
{
	private static String describe(String s)
	{
		return "string:" + s;
	}

	private static String describe(int i)
	{
		return "int:" + i;
	}

	public static void main(String[] args)
	{
		int n = 5;
		System.out.println(describe("idx=" + n));
	}
}
