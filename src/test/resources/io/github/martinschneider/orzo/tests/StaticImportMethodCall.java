package io.github.martinschneider.orzo.tests;

import static io.github.martinschneider.orzo.tests.StaticImportFieldHolder.NAMES;
import static io.github.martinschneider.orzo.tests.StaticImportFieldHolder.twice;

public class StaticImportMethodCall
{
	public static void main(String[] args)
	{
		System.out.println(NAMES.contains("b"));
		System.out.println(NAMES.contains("z"));
		System.out.println(NAMES.size());
		// a statically imported method of a class of the same compilation run
		System.out.println(twice("ab"));
	}
}
