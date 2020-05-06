package cc.mschneider.kommpeiler.codegen;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import cc.mschneider.kommpeiler.codegen.constants.ConstantDouble;
import cc.mschneider.kommpeiler.codegen.constants.ConstantPool;
import cc.mschneider.kommpeiler.codegen.constants.ConstantUtf8;


public class CodeGenerator
{
    private static final short JAVA_CLASS_MAJOR_VERSION = 50;
    private static final short JAVA_CLASS_MINOR_VERSION = 0;

    private ConstantPool constants = new ConstantPool();
    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private DataOutputStream clazz = new DataOutputStream(bytes);

    public static void main(String args[]) throws IOException
    {
        byte[] clazz = new CodeGenerator().generateJavaClassFile();

        FileOutputStream fos = new FileOutputStream("TestProgram.class");

        fos.write(clazz);
        fos.close();
    }

    public byte[] generateJavaClassFile() throws IOException
    {
        // magic number
        clazz.writeInt(0xCAFEBABE);

        // class file version
        clazz.write(shortToByteArray(JAVA_CLASS_MINOR_VERSION));
        clazz.write(shortToByteArray(JAVA_CLASS_MAJOR_VERSION));

        // constant pool
        constants.addEntry(new ConstantUtf8("TestProgram"));
        constants.addEntry(new ConstantDouble(-13.9));
        clazz.write(constants.getBytes());

        // constants.addEntry(new Class(1));
        
        

        clazz.flush();

        System.out.println(byteArrayToString(bytes.toByteArray()));

        return bytes.toByteArray();
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static String byteArrayToString(byte[] array)
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
            else if ((i + 1) % 2 == 0 && i != array.length - 1)
            {
                sb.append(" | ");
            }
        }
        return new String(sb.toString());
    }

    public static byte[] shortToByteArray(short value)
    {
        return new byte[]
        {(byte) ((value >> 8) & 0xff), (byte) (value & 0xff)};
    }

    public static byte[] intToByteArray(int value)
    {
        return new byte[]
        {(byte) ((value >> 24) & 0xff), (byte) ((value >> 16) & 0xff), (byte) ((value >> 8) & 0xff),
         (byte) (value & 0xff)};
    }

    public static byte[] longToByteArray(long value)
    {
        return new byte[]
        {(byte) ((value >> 56) & 0xff), (byte) ((value >> 48) & 0xff), (byte) ((value >> 40) & 0xff),
         (byte) ((value >> 32) & 0xff), (byte) ((value >> 24) & 0xff), (byte) ((value >> 16) & 0xff),
         (byte) ((value >> 8) & 0xff), (byte) (value & 0xff)};
    }
}