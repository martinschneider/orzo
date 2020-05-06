package cc.mschneider.kommpeiler.scanner.tokens;

/**
 * IntNum
 * 
 * @author Martin Schneider
 */
public class IntNum extends Token implements Num
{
    /**
     * @param value value
     */
    public IntNum(final String value)
    {
        super(value);
    }

    /**
     * @return int-value
     */
    public int parseValue()
    {
        return Integer.parseInt(getValue());
    }

    /**{@inheritDoc}**/
    public String toString()
    {
        return "NUM(" + getValue() + ")";
    }

}
