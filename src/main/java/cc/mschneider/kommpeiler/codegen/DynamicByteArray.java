package cc.mschneider.kommpeiler.codegen;

public class DynamicByteArray
{
    private int size = 2;
    private byte[] array = new byte[size];
    private int pointer = 0;

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public void write(byte b)
    {
        if (pointer >= size)
        {
            size *= 2;
            byte[] help = new byte[size];
            for (int i = 0; i < size / 2; i++)
            {
                help[i] = array[i];
            }
            array = help;
        }
        array[pointer] = b;
        pointer++;
    }

    public void write(byte[] b)
    {
        while (pointer + b.length > size)
        {
            size *= 2;
            byte[] help = new byte[size];
            for (int i = 0; i < size / 2; i++)
            {
                help[i] = array[i];
            }
            array = help;
        }
        for (int i = 0; i < b.length; i++)
        {
            array[pointer] = b[i];
            pointer++;
        }
    }

    public byte[] getBytes()
    {
        byte[] retValue = new byte[pointer];
        for (int i=0; i<pointer;i++)
        {
            retValue[i]=array[i];
        }
        return retValue;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pointer; i++)
        {
            sb.append(HEX_CHARS[(array[i] & 0xF0) >>> 4]);
            sb.append(HEX_CHARS[array[i] & 0x0F]);
            if ((i + 1) % 16 == 0)
            {
                sb.append("\n");
            }
            else if ((i + 1) % 2 == 0 && i!=pointer-1)
            {
                sb.append(" | ");
            }
        }
        return new String(sb.toString());
    }
}