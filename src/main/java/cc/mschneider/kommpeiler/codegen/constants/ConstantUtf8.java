package cc.mschneider.kommpeiler.codegen.constants;

import java.io.UnsupportedEncodingException;

public class ConstantUtf8 implements Constant
{
    private String value;

    public ConstantUtf8(String value)
    {
        this.value=value;
    }

    @Override
    public byte[] getInfo()
    {
        try
        {
            return value.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return value.getBytes();
        }
    }

    @Override
    public byte getTag()
    {
        return ConstantTypes.CONSTANT_UTF8;
    }
}
