package io.github.martinschneider.orzo.tests;

public class EmbeddedFieldAssignment
{
	private char character;
	private static int counter;

	public void scanInstance()
	{
		if ((character = 'x') == 'x')
		{
			System.out.println(character);
		}
	}

	public static void scanStatic()
	{
		if ((counter = 5) == 5)
		{
			System.out.println(counter);
		}
	}

	public static void main(String[] args)
	{
		new EmbeddedFieldAssignment().scanInstance();
		scanStatic();
	}
}
