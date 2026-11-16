package io.github.martinschneider.orzo.tests;

public class MethodThrowsClause
{
	public static int risky(int n) throws Exception
	{
		return n * 2;
	}

	public static int overload(int n) throws Exception
	{
		return n;
	}

	public static int overload(String s) throws java.io.IOException, RuntimeException
	{
		return s.length();
	}

	public static int divide(int a, int b)
	{
		try
		{
			return a / b;
		}
		catch (ArithmeticException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println(risky(5));
		System.out.println(overload(3));
		System.out.println(overload("hello"));
		System.out.println(divide(10, 2));
	}
}
