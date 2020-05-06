package cc.mschneider.kommpeiler.scanner.tokens;

/**
 * Str
 * 
 * @author Martin Schneider
 */

public class Str extends Token
{
    /**
     * @param value value
     */
    public Str(final String value)
    {
        super(value);
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return "STR(" + getValue() + ")";
    }
}
