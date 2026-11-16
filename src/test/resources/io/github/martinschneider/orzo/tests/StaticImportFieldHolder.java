package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class StaticImportFieldHolder
{
	public static final List<String> NAMES = buildNames();

	private static List<String> buildNames()
	{
		List<String> names = new ArrayList<>();
		names.add("a");
		names.add("b");
		names.add("c");
		return names;
	}
}
