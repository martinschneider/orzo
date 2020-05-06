package cc.mschneider.kommpeiler.error;

/**
 * ParserException
 * 
 * @author Martin Schneider
 */
public class ParserException extends Exception
{

    private static final long serialVersionUID = 3816586511828227991L;

    /**
     * @param message message
     */
    public ParserException(final String message)
    {
        super(message);
    }
}
