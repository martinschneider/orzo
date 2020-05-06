package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;


/**
 * ConditionalStatement
 * @author Martin Schneider
 */
public abstract class ConditionalStatement extends Statement
{

    private Factor condition;
    private List<Assignment> body;
    
    /**
     * @param left left
     * @param right right
     */
    public ConditionalStatement(final Identifier left, final Factor right)
    {
        super();
    }

    /**
     * empty constructor
     */
    public ConditionalStatement()
    {
    }

    /**
     * set the condition
     * @param condition condition
     */
    void setCondition(final Factor condition)
    {
        this.condition=condition;
    }

    /**
     * @return condition
     */
    Factor getCondition()
    {
        return condition;
    }

    /**
     * set the body
     * @param body body
     */
    void setBody(final List<Assignment> body)
    {
        this.body=body;
    }

    /**
     * @return body
     */
    List<Assignment> getBody()
    {
        return body;
    }

}