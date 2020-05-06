package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

/**
 * IfStatement
 */
public class IfStatement extends ConditionalStatement
{
    private Factor condition;
    private List<Assignment> body;
    
    /**
     * @param condition condition
     * @param body body
     */
    public IfStatement(final Factor condition, final List<Assignment> body)
    {
        super();
        this.condition = condition;
        this.body = body;
    }
    
    public void setCondition(final Factor condition)
    {
        this.condition = condition;
    }
    public Factor getCondition()
    {
        return condition;
    }
    public void setBody(final List<Assignment> body)
    {
        this.body = body;
    }
    public List<Assignment> getBody()
    {
        return body;
    }
}
