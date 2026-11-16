package io.github.martinschneider.orzo.tests;

public class CharOverloadResolution
{
	public static void main(String[] args)
	{
		char c = 'A';
		StringBuffer buffer = new StringBuffer();
		buffer.append(c);
		System.out.println(buffer.toString());
	}
}
