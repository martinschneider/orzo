package io.github.martinschneider.orzo.tests;

public class VariableShadowsField
{
	private char character = 'F';

	public void scan()
	{
		char character;
		character = 'L';
		System.out.println("local=" + character);
		System.out.println("field=" + this.character);
	}

	public static void main(String[] args)
	{
		new VariableShadowsField().scan();
	}
}
