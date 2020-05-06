package cc.mschneider.kommpeiler.error;

/**
 * Scanner exception
 * 
 * @author Martin Schneider
 */
public class ScannerException extends Exception
{

    private static final long serialVersionUID = -7250895231248049104L;

    /**
     * @param message message
     */
    public ScannerException(final String message)
    {
        super(message);
    }
}
