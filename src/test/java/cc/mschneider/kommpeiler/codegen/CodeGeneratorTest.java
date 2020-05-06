package cc.mschneider.kommpeiler.codegen;

import org.junit.Assert;
import org.junit.Test;

import cc.mschneider.kommpeiler.codegen.CodeGenerator;
import cc.mschneider.kommpeiler.codegen.constants.ConstantDouble;
import cc.mschneider.kommpeiler.codegen.constants.ConstantUtf8;


public class CodeGeneratorTest
{
    private CodeGenerator codeGenerator = new CodeGenerator();
    
    @Test
    public void testConstantUtf8()
    {
        byte[] array = CodeGenerator.longToByteArray(0x736f6d6553747269l);
        Assert.assertArrayEquals(array,new ConstantUtf8("someStri").getInfo());
    }
    
    @Test
    public void testConstantDouble()
    {    
        byte[] array = CodeGenerator.longToByteArray(0xc02bcccccccccccdl);
        Assert.assertArrayEquals(array,new ConstantDouble(-13.9).getInfo());
    }

    
    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();
    public String byteArrayToString(byte[] array)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; i++)
        {
            sb.append(HEX_CHARS[(array[i] & 0xF0) >>> 4]);
            sb.append(HEX_CHARS[array[i] & 0x0F]);
            if ((i + 1) % 16 == 0)
            {
                sb.append("\n");
            }
            else if ((i + 1) % 2 == 0 && i!=array.length-1)
            {
                sb.append(" | ");
            }
        }
        return new String(sb.toString());
    }
}
