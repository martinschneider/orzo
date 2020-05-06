package cc.mschneider.kommpeiler.parser.productions;

/**
 * Factor
 * 
 * @author Martin Schneider
 */
public interface Factor
{
  
    /**
     * @return value
     */
    Object getValue();
    
    /**
     * @return adress
     */
    int getAdress();
    
    /**
     * @return register nr
     */
    int getRegisterNr();
    
    /**
     * @return value type
     */
    ValueType getValueType();
    
    /**
     * @param nr registerNr
     */
    void setRegisterNr(int nr);
    
    /**
     * @param valueType valueType
     */
    void setValueType(ValueType valueType);
    
    /**
     * @param adress memory adress
     */
    void setAdress(int adress);
}