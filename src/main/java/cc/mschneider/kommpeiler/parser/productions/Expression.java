package cc.mschneider.kommpeiler.parser.productions;

import cc.mschneider.kommpeiler.scanner.tokens.Token;

/**
 * Expression
 * 
 * @author Martin Schneider
 */
public class Expression extends SimpleExpression
{
    /**
     * @param left left side of expression
     * @param operator operator
     * @param right right side of expression
     */
    public Expression(final Factor left, final Token operator, final Factor right)
    {
        super(left, operator, right);
    }

    /**
     * empty constructor
     */
    public Expression()
    {
       super();
    }
    
    public Object getValue()
    {
        return null;
    }
}
