package cc.mschneider.kommpeiler.parser.productions;

/**
 * ExpressionFactor
 * 
 * @author Martin Schneider
 */
public class ExpressionFactor implements Factor
{
    private ValueType valueType = ValueType.UNKNOWN;
    
    private int adress;
    
    private int registerNr;
    
    private Factor expressionValue;

    public Factor getExpressionValue()
    {
        return expressionValue;
    }

    public Object getValue()
    {
        this.valueType=expressionValue.getValueType();
        return expressionValue.getValue();
    }
    
    public int getIntValue()
    {
        return (Integer)expressionValue.getValue();
    }
    
    public void setValue(final Factor value)
    {
        this.expressionValue = value;
    }

    /**
     * empty constructor
     */
    public ExpressionFactor()
    {
    }

    /**
     * @param value value
     */
    public ExpressionFactor(final Factor value)
    {
        this.expressionValue = value;
    }
    
    /** {@inheritDoc} **/
    public String toString()
    {
        return expressionValue.toString();
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
        this.valueType=valueType;
    }

}
