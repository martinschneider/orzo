package cc.mschneider.kommpeiler.parser.productions;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;
import cc.mschneider.kommpeiler.scanner.tokens.Token;

/**
 * IdFactor
 * 
 * @author Martin Schneider
 */
public class IdFactor implements Factor
{

    private Selector selector;
    
    private Token token;

    private ValueType valueType = ValueType.MEMORY;
    
    private int adress;
    
    private int registerNr;
    
    /**
     * empty constructor
     */
    public IdFactor()
    {
    }
    
    public Object getValue()
    {
        return token.getValue();
    }

    /**
     * @param token token
     */
    public IdFactor(final Token token)
    {
        this.setToken(token);
        if (token instanceof Identifier)
        {
            this.setSelector(((Identifier) token).getSelector());
        }
    }

    public void setToken(final Token token)
    {
        this.token = token;
    }

    public Token getToken()
    {
        return token;
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return token.toString();
    }

    public void setSelector(final Selector selector)
    {
        this.selector = selector;
    }

    public Selector getSelector()
    {
        return selector;
    }
    
    public void setAdress(final int adress)
    {
        this.adress = adress;
    }

    /**{@inheritDoc}*/
    public int getAdress()
    {
        if (token instanceof Identifier)
        {
            return ((Identifier)token).getAdress();
        }
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
