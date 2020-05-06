package cc.mschneider.kommpeiler.codegen.constants;

import java.io.IOException;

import cc.mschneider.kommpeiler.codegen.CodeGenerator;


public class ConstantClass implements Constant
{
    private short id;
    
    public ConstantClass(short id)
    {
        this.id=id;
    }
    
    @Override
    public byte[] getInfo()
    {
        return CodeGenerator.shortToByteArray(id);
    }

    @Override
    public byte getTag()
    {
        return ConstantTypes.CONSTANT_CLASS;
    }
}