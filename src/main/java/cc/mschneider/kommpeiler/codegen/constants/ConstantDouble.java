package cc.mschneider.kommpeiler.codegen.constants;

import cc.mschneider.kommpeiler.codegen.CodeGenerator;

public class ConstantDouble implements Constant
{
    private double value;
    
    public ConstantDouble(double value)
    {
        this.value=value;
    }

    @Override
    public byte[] getInfo()
    {
        return CodeGenerator.longToByteArray(Double.doubleToLongBits(value));
    }

    @Override
    public byte getTag()
    {
        return ConstantTypes.CONSTANT_DOUBLE;
    }
}
