package io.github.martinschneider.orzo.tests;

public class ChainedConstructorStatement
{
	private int value;

	public ChainedConstructorStatement set(int v)
	{
		value = v;
		return this;
	}

	public void show()
	{
		System.out.println(value);
	}

	public static void main(String[] args)
	{
		new ChainedConstructorStatement().set(42).show();
		new StringBuilder().append("a").append("b");
	}
}
