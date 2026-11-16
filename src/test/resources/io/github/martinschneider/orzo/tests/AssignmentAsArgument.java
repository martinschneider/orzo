package io.github.martinschneider.orzo.tests;

public class AssignmentAsArgument
{
	private static boolean isDigit(char c)
	{
		return c >= '0' && c <= '9';
	}

	public static void main(String[] args)
	{
		char character;
		if (isDigit(character = '7'))
		{
			System.out.println(character);
		}
		int a;
		int b = (a = 5) + 1;
		System.out.println(a);
		System.out.println(b);
	}
}
