package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

/**
 * DoStatement
 */
public class DoStatement extends ConditionalStatement
{
    private Factor condition;
    private List<Assignment> body;

    /**
     * @param condition condition
     * @param body body
     */
    public DoStatement(final Factor condition, final List<Assignment> body)
    {
        this.condition = condition;
        this.body = body;
    }

    /** {@inheritDoc} **/
    public void setCondition(final Factor condition)
    {
        this.condition = condition;
    }

    /** {@inheritDoc} **/
    public Factor getCondition()
    {
        return condition;
    }

    /** {@inheritDoc} **/
    public void setBody(final List<Assignment> body)
    {
        this.body = body;
    }

    /** {@inheritDoc} **/
    public List<Assignment> getBody()
    {
        return body;
    }
}
