package cc.mschneider.kommpeiler.scanner.tokens;

/**
 * DoubleNum
 * 
 * @author Martin Schneider
 */
public class DoubleNum extends Token implements Num
{
    /**
     * @param value value
     */
    public DoubleNum(final String value)
    {
        super(value);
    }

    /**
     * @return double-value
     */
    public double parseValue()
    {
        return Double.parseDouble(getValue());
    }

    /**{@inheritDoc}**/
    public String toString()
    {
        return "NUM(" + getValue() + ")";
    }

}
