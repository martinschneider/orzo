package cc.mschneider.kommpeiler.parser.productions;

import java.util.List;

import cc.mschneider.kommpeiler.codegen.CodeGenerator;
import cc.mschneider.kommpeiler.scanner.tokens.Identifier;
import cc.mschneider.kommpeiler.scanner.tokens.Token;

//import at.mschneider.kommpeiler.codegen.Instruction;
//import at.mschneider.kommpeiler.codegen.RegisterList;

/**
 * Term term = factor {("*" | "/" | "%") factor}
 * @author Martin Schneider
 */
public class Term extends IntFactor
{
    private Factor left;
    private Token operator;
    private Factor right;

    private Object termValue;
    private ValueType valueType = ValueType.UNKNOWN;

    /**
     * empty constructor
     */
    public Term()
    {
    };

    /**
     * @param left left side of term
     * @param operator operator
     * @param right right side of term
     */
    public Term(final Factor left, final Token operator, final Factor right)
    {
        this.left = left;
        this.operator = operator;
        this.right = right;
        getValue();
    }

    /**
     * Sets the value on the left side of the operator (and resets the term's value)
     * @param left left
     */
    public void setLeft(final Factor left)
    {
        this.left = left;
        termValue = null;
    }

    public Factor getLeft()
    {
        return left;
    }

    /**
     * Sets the operator (and resets the value)
     * @param operator operator
     */
    public void setOperator(final Token operator)
    {
        this.operator = operator;
        termValue = null;
    }

    public Token getOperator()
    {
        return operator;
    }

    /**
     * Sets the value on the right side of the operator (and resets the term's value)
     * @param right right
     */
    public void setRight(final Factor right)
    {
        this.right = right;
        termValue = null;
    }

    public Factor getRight()
    {
        return right;
    }

    /** {@inheritDoc} **/
    public String toString()
    {
        return left.toString() + " " + operator.toString() + " " + right.toString();
    }

    /** {@inheritDoc} */
    public Object getValue()
    {
        if (this.getLeft() instanceof ExpressionFactor)
        {
            this.getLeft().getValue();
        }
        if (this.getRight() instanceof ExpressionFactor)
        {
            this.getRight().getValue();
        }
        if (termValue != null)
        {
            return termValue;
        }
        else if (this.getLeft().getValueType().equals(ValueType.IMMEDIATE)
                 && this.getRight().getValueType().equals(ValueType.IMMEDIATE))
        {
            int value = -1;
            if (this.getOperator().getValue().equals("TIMES"))
            {
                if (this.getLeft() instanceof IntFactor)
                {
                    value = ((IntFactor) this.getLeft()).getIntValue() * ((IntFactor) this.getRight()).getIntValue();
                }
                else if (this.getLeft() instanceof ExpressionFactor)
                {
                    value = ((ExpressionFactor) this.getLeft()).getIntValue()
                            * ((IntFactor) this.getRight()).getIntValue();
                }
                this.setValue(value);
                this.setValueType(ValueType.IMMEDIATE);
                return value;
            }
            else if (this.getOperator().getValue().equals("DIV"))
            {
                value = ((IntFactor) this.getLeft()).getIntValue() / ((IntFactor) this.getRight()).getIntValue();
                this.setValue(value);
                this.setValueType(ValueType.IMMEDIATE);
                return value;
            }
            else if (this.getOperator().getValue().equals("MOD"))
            {
                value = ((IntFactor) this.getLeft()).getIntValue() % ((IntFactor) this.getRight()).getIntValue();
                this.setValue(value);
                this.setValueType(ValueType.IMMEDIATE);
                return value;
            }
        }
        // else
        return null;
    }

//    /**
//     * @param instructionList instructionList
//     * @param registers registers
//     */
//    // CHECKSTYLE:OFF
//    public void generateCode(List<Instruction> instructionList, RegisterList registers)
//    // CHECKSTYLE:ON
//    {
//        if (this.getLeft() instanceof Term)
//        {
//            ((Term) this.getLeft()).generateCode(instructionList, registers);
//        }
//        if (this.getRight() instanceof Term)
//        {
//            ((Term) this.getRight()).generateCode(instructionList, registers);
//        }
//
//        // constant folding
//        getValue();
//
//        int cmd = CodeGenerator.getOp(this.getOperator());
//
//        // if the second operand is immediate, we can use the immediate version of MUL, DIV or MOD
//        if (this.getRight().getValueType().equals(ValueType.IMMEDIATE))
//        {
//            cmd = CodeGenerator.getImmediateOp(cmd);
//        }
//
//        // REGISTER x REGISTER
//        if (this.getLeft().getValueType().equals(ValueType.REGISTER)
//            && this.getRight().getValueType().equals(ValueType.REGISTER))
//        {
//            instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(),
//                                                this.getRight().getRegisterNr()));
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//
//        // IMMEDIATE x REGISTER
//        else if (this.getLeft().getValueType().equals(ValueType.IMMEDIATE)
//                 && this.getRight().getValueType().equals(ValueType.REGISTER))
//        {
//            if (cmd==Command.MUL || cmd==Command.ADD) // MUL AND ADD are commutative
//            {
//                cmd = CodeGenerator.getImmediateOp(cmd);
//                instructionList.add(new Instruction(cmd,
//                                                    this.getRight().getRegisterNr(), this.getRight().getRegisterNr(),
//                                                     (Integer)this.getLeft().getValue()));
//                this.setValueType(ValueType.REGISTER);
//                this.setRegisterNr(this.getRight().getRegisterNr());
//                this.getRight().setValueType(ValueType.MEMORY);
//            }
//            else
//            // DIV, SUB and MOD are not
//            {
//                CodeGenerator.loadIntoRegister(instructionList, registers, this.getLeft());
//
//                instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), this.getRight().getRegisterNr()));
//
//                this.setValueType(ValueType.REGISTER);
//                this.setRegisterNr(this.getLeft().getRegisterNr());
//                this.getLeft().setValueType(ValueType.MEMORY);
//            }
//        }
//
//        // MEMORY x IMMEDIATE
//        else if (this.getLeft().getValueType().equals(ValueType.MEMORY)
//                 && this.getRight().getValueType().equals(ValueType.IMMEDIATE))
//        {
//            CodeGenerator.loadWord(instructionList, registers, this.getLeft());
//
//            instructionList.add(new Instruction(cmd,
//            this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), (Integer)this.getRight().getValue()));
//
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//
//        // IMMEDIATE x MEMORY
//        else if (this.getLeft().getValueType().equals(ValueType.IMMEDIATE)
//                 && this.getRight().getValueType().equals(ValueType.MEMORY))
//        {
//            if (cmd==Command.MUL || cmd==Command.ADD)
//            {
//                cmd = CodeGenerator.getImmediateOp(cmd);
//                CodeGenerator.loadWord(instructionList, registers, this.getRight());
//                instructionList.add(new Instruction(cmd,
//                                                    this.getRight().getRegisterNr(), this.getRight().getRegisterNr(),
//                                                     (Integer)this.getLeft().getValue()));
//                this.setValueType(ValueType.REGISTER);
//                this.setRegisterNr(this.getRight().getRegisterNr());
//                this.getRight().setValueType(ValueType.MEMORY);
//            }
//            else
//            {
//                CodeGenerator.loadIntoRegister(instructionList, registers, this.getLeft());
//                CodeGenerator.loadWord(instructionList, registers, this.getRight());
//                instructionList.add(new Instruction(cmd, 
//                this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), this.getRight().getRegisterNr()));
//                this.setValueType(ValueType.REGISTER);
//                this.setRegisterNr(this.getLeft().getRegisterNr());
//                this.getLeft().setValueType(ValueType.MEMORY);
//            }
//        }
//
//        // MEMORY x MEMORY
//        else if (this.getLeft().getValueType().equals(ValueType.MEMORY)
//                 && this.getRight().getValueType().equals(ValueType.MEMORY))
//        {
//            CodeGenerator.loadWord(instructionList, registers, this.getLeft());
//            CodeGenerator.loadWord(instructionList, registers, this.getRight());
//
//            instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), this.getRight().getRegisterNr()));
//
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//
//        // MEMORY x REGISTER
//        else if (this.getLeft().getValueType().equals(ValueType.MEMORY)
//                 && this.getRight().getValueType().equals(ValueType.REGISTER))
//        {
//            CodeGenerator.loadWord(instructionList, registers, this.getLeft());
//
//            instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), this.getRight().getRegisterNr()));
//
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//
//        // REGISTER x MEMORY
//        else if (this.getLeft().getValueType().equals(ValueType.REGISTER)
//                 && this.getRight().getValueType().equals(ValueType.MEMORY))
//        {
//            CodeGenerator.loadWord(instructionList, registers, this.getRight());
//
//            instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), this.getRight().getRegisterNr()));
//
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//
//        // REGISTER x IMMEDIATE
//        else if (this.getLeft().getValueType().equals(ValueType.REGISTER)
//                 && this.getRight().getValueType().equals(ValueType.IMMEDIATE))
//        {
//            instructionList.add(new Instruction(cmd, this.getLeft().getRegisterNr(), this.getLeft().getRegisterNr(), (Integer)this.getRight().getValue()));
//
//            this.setValueType(ValueType.REGISTER);
//            this.setRegisterNr(this.getLeft().getRegisterNr());
//            this.getLeft().setValueType(ValueType.MEMORY);
//        }
//    }

    public void setValueType(final ValueType valueType)
    {
        this.valueType = valueType;
    }

    public ValueType getValueType()
    {
        return valueType;
    }
}
