package cc.mschneider.kommpeiler.parser.productions;

/**
 * IntFactor
 * 
 * @author Martin Schneider
 */
public class IntFactor implements Factor
{
    private int value;
    
    private ValueType valueType = ValueType.IMMEDIATE;
    
    private int adress;
    
    private int registerNr;

    public Object getValue()
    {
        return value;
    }
    
    public int getIntValue()
    {
        return value;
    }

    public void setValue(final Object value)
    {
        this.value = ((Integer)value).intValue();
    }

    /**
     * empty constructor
     */
    public IntFactor()
    {
    }

    /**
     * @param value value
     */
    public IntFactor(final int value)
    {
        this.value = value;
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return Integer.toString(value);
    }
    
    public void setAdress(final int adress)
    {
        this.adress = adress;
    }

    public int getAdress()
    {
        return adress;
    }

    public void setRegisterNr(final int registerNr)
    {
        this.registerNr = registerNr;
    }

    public int getRegisterNr()
    {
        return registerNr;
    }
    
    public ValueType getValueType()
    {
        return valueType;
    }
    
    public void setValueType(final ValueType valueType)
    {
        this.valueType = valueType;
    }
}
