package cc.mschneider.kommpeiler.scanner.tokens;

/**
 * Token
 * 
 * @author Martin Schneider
 */

public class Token
{
    private String value;

    /**
     * @param value value
     */
    public Token(final String value)
    {
        this.value = value;
    }

    public void setValue(final String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    /**{@inheritDoc}**/
    public String toString()
    {
        return value;
    }
}
