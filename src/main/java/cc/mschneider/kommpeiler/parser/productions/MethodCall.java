package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.scanner.tokens.Identifier;


/**
 * MethodCall
 * @author Martin Schneider
 */
public class MethodCall
{
    private Identifier name;

    /**
     * @param name name
     * @param parameters list of parameters
     */
    public MethodCall(final Identifier name, final List<Factor> parameters)
    {
        super();
        this.name = name;
        this.parameters = parameters;
    }

    private List<Factor> parameters;

    public Identifier getName()
    {
        return name;
    }

    public void setName(final Identifier name)
    {
        this.name = name;
    }

    public List<Factor> getParameters()
    {
        return parameters;
    }

    public void setParameters(final List<Factor> parameters)
    {
        this.parameters = parameters;
    }
}
