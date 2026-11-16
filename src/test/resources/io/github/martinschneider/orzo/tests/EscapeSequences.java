package io.github.martinschneider.orzo.tests;

public class EscapeSequences
{
	public static void main(String[] args)
	{
		char a = '\'';
		char b = '"';
		char c = '\\';
		char d = '\n';
		char e = '\t';
		System.out.println(a);
		System.out.println(b);
		System.out.println((int) c);
		System.out.println((int) d);
		System.out.println((int) e);
		String s = "say \"hi\"";
		String t = "back\\slash";
		System.out.println(s);
		System.out.println(t);
	}
}
