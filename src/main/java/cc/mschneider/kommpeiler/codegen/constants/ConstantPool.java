package cc.mschneider.kommpeiler.codegen.constants;

import java.util.ArrayList;
import java.util.List;

import cc.mschneider.kommpeiler.codegen.DynamicByteArray;


public class ConstantPool
{
    public ConstantPool()
    {
        entries = new ArrayList<Constant>();
    }
    
    private List<Constant> entries;

    public void setEntries(final List<Constant> entries)
    {
        this.entries = entries;
    }

    public List<Constant> getEntries()
    {
        return entries;
    }

    public byte[] getBytes()
    {
        DynamicByteArray array = new DynamicByteArray();
        array.write((byte)((entries.size()+1 >> 8)& 0xFF));
        array.write((byte)(entries.size() & 0xFF));
        
        for (Constant constant : entries)
        {
            array.write(constant.getTag());
            array.write(constant.getInfo());
        }
        return array.getBytes();
    }

    public void addEntry(Constant entry)
    {
        entries.add(entry);
    }
}