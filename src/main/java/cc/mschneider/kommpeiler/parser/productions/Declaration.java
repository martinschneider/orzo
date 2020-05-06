package cc.mschneider.kommpeiler.parser.productions;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;

/**
 * Declaration
 * 
 * @author Martin Schneider
 */
public class Declaration extends Statement
{
    /**
     * @param name name
     * @param type type
     * @param value value
     * @param hasValue true if value is known
     */
    public Declaration(final Identifier name, final String type, final Factor value, final boolean hasValue)
    {
        super();
        this.type = type;
        this.name = name;
        this.value = value;
        this.hasValue = hasValue;
    }

    private String type;

    private Identifier name;

    private Factor value;

    private boolean hasValue;

    public String getType()
    {
        return type;
    }

    public void setType(final String type)
    {
        this.type = type;
    }

    public Identifier getName()
    {
        return name;
    }

    public void setName(final Identifier name)
    {
        this.name = name;
    }

    public Factor getValue()
    {
        return value;
    }

    public void setValue(final Factor value)
    {
        this.value = value;
    }

    public void setHasValue(final boolean hasValue)
    {
        this.hasValue = hasValue;
    }

    /**
     * @return true if value is known
     */
    public boolean hasValue()
    {
        return hasValue;
    }
}
