package io.github.martinschneider.orzo.tests;

// TODO: disabled — a local variable shadowing a field of the same name resolves inconsistently
// (sometimes the field, sometimes the local, depending on codegen path). Root-caused while
// self-compiling Lexer.java (scanOps() declares "char character;" locally inside one branch,
// shadowing the field "character" used everywhere else in the class) but not yet fixed — this
// is a general, pre-existing identifier-resolution gap, not specific to embedded assignment.
// Kept as reference for what still needs fixing; see docs/TODO.md.
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
