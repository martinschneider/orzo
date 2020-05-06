package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;


/**
 * Method
 * @author Martin Schneider
 */
public class Method
{
    private Scope scope;
    private BasicType type;
    private Identifier name;
    private List<Assignment> body;

    /**
     * @param scope scope
     * @param type return-type
     * @param name name
     * @param body method-body
     */
    public Method(final Scope scope, final BasicType type, final Identifier name, final List<Assignment> body)
    {
        this.scope = scope;
        this.type = type;
        this.name = name;
        this.body = body;
    }

    public Scope getScope()
    {
        return scope;
    }

    public void setScope(final Scope scope)
    {
        this.scope = scope;
    }

    public BasicType getType()
    {
        return type;
    }

    public void setType(final BasicType type)
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

    public List<Assignment> getBody()
    {
        return body;
    }

    public void setBody(final List<Assignment> body)
    {
        this.body = body;
    }
}
