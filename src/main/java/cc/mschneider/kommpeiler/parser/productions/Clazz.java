package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;


/**
 * Class
 * @author Martin Schneider
 */
public class Clazz
{
    private Scope scope;
    private Identifier name;
    private List<Method> body;

    /**
     * @param scope scope
     * @param name name
     * @param body body
     */
    public Clazz(final Scope scope, final Identifier name, final List<Method> body)
    {
        this.scope = scope;
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
    public Identifier getName()
    {
        return name;
    }
    public void setName(final Identifier name)
    {
        this.name = name;
    }
    public List<Method> getBody()
    {
        return body;
    }
    public void setBody(final List<Method> body)
    {
        this.body = body;
    }
}
