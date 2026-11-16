package io.github.martinschneider.orzo.tests;

import static io.github.martinschneider.orzo.tests.StaticImportFieldHolder.NAMES;

public class StaticImportMethodCall
{
	public static void main(String[] args)
	{
		System.out.println(NAMES.contains("b"));
		System.out.println(NAMES.contains("z"));
		System.out.println(NAMES.size());
	}
}
