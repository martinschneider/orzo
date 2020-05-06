package cc.mschneider.kommpeiler.parser.productions;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;

/**
 * Statement
 * @author Martin Schneider
 */
public class Statement extends Assignment
{

    /**
     * empty constructor
     */
    public Statement()
    {
        super();
    }

    /**
     * @param left left side of statement
     * @param right right side of statement
     */
    public Statement(final Identifier left, final Factor right)
    {
        super(left, right);
    }

}
