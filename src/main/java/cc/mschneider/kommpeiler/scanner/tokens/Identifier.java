package cc.mschneider.kommpeiler.scanner.tokens;

import cc.mschneider.kommpeiler.parser.productions.Selector;

/**
 * Identifier
 * @author Martin Schneider
 */
public class Identifier extends Token
{
    private Selector selector;

    private int adress;

    public int getAdress()
    {
        return adress;
    }

    public void setAdress(final int adress)
    {
        this.adress = adress;
    }

    /**
     * @param value value
     */
    public Identifier(final String value)
    {
        super(value);
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return getValue();
    }

    public void setSelector(final Selector selector)
    {
        this.selector = selector;
    }

    public Selector getSelector()
    {
        return selector;
    }
}
