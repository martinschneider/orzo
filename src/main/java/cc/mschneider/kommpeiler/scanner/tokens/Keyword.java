package cc.mschneider.kommpeiler.scanner.tokens;

/**
 * Keyword
 * 
 * @author Martin Schneider
 */
public class Keyword extends Token
{
    /**
     * @param value value
     */
    public Keyword(final String value)
    {
        super(value.toUpperCase());
    }
}
