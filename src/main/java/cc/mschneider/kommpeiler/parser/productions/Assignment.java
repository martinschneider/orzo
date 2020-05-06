package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.codegen.CodeGenerator;
import cc.mschneider.kommpeiler.scanner.tokens.Identifier;

//import at.mschneider.kommpeiler.codegen.Instruction;
//import at.mschneider.kommpeiler.codegen.RegisterList;

/**
 * Assignment
 * @author Martin Schneider
 */
public class Assignment
{

    private Identifier left;
    private Factor right;

    /**
     * empty constructor
     */
    public Assignment()
    {
    }

    /**
     * @param left left side of assignment
     * @param right right side of assignment
     */
    public Assignment(final Identifier left, final Factor right)
    {
        this.setLeft(left);
        this.setRight(right);
    }

    public void setLeft(final Identifier left)
    {
        this.left = left;
    }

    public Identifier getLeft()
    {
        return left;
    }

    public void setRight(final Factor right)
    {
        this.right = right;
    }

    public Factor getRight()
    {
        return right;
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return left.toString() + ":=" + right.toString();
    }

//    /**
//     * @param instructionList instructionList
//     * @param registers registers
//     */
//    public void generateCode(final List<Instruction> instructionList, final RegisterList registers)
//    {
//        if (right.getValueType() == ValueType.IMMEDIATE)
//        {
//            CodeGenerator.loadIntoRegister(instructionList, registers, right);
//        }
//        else if (right.getValueType() == ValueType.MEMORY)
//        {
//            CodeGenerator.loadWord(instructionList, registers, right);
//        }
//        CodeGenerator.storeWord(instructionList, registers, right.getRegisterNr(), left);
//    }

}
