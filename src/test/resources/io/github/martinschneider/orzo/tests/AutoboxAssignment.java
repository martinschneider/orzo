package io.github.martinschneider.orzo.tests;

public class AutoboxAssignment
{
	static Integer field;

	public static void main(String[] args)
	{
		Integer y = 3;
		System.out.println(y);
		y = 7;
		System.out.println(y);
		field = 9;
		System.out.println(field);
		Long l = 10L;
		System.out.println(l);
	}
}