package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFEQ;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFGE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFGT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFLE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFLT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IFNE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPEQ;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPGE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPGT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPLE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPLT;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IF_ICMPNE;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class ConditionalCodeGenerator {
  private ExpressionCodeGenerator ecg;

  public void setExpressionCodeGenerator(ExpressionCodeGenerator ecg) {
    this.ecg = ecg;
  }

  public HasOutput generateCondition(
      DynamicByteArray out,
      Clazz clazz,
      Map<Identifier, Integer> variables,
      ConstantPool constantPool,
      Condition condition,
      short branchBytes) {
    return generateCondition(out, clazz, variables, constantPool, condition, branchBytes, false);
  }

  public HasOutput generateCondition(
      DynamicByteArray out,
      Clazz clazz,
      Map<Identifier, Integer> variables,
      ConstantPool constantPool,
      Condition condition,
      short branchBytes,
      boolean isDoLoop) {
    DynamicByteArray conditionOut = new DynamicByteArray();
    // TODO: support other boolean conditions
    ExpressionResult left =
        ecg.evaluateExpression(conditionOut, variables, condition.getLeft(), false);
    ExpressionResult right =
        ecg.evaluateExpression(conditionOut, variables, condition.getRight(), false);
    boolean leftZero = isZero(left.getValue());
    boolean rightZero = isZero(right.getValue());
    if (leftZero ^ rightZero) {
      boolean reverseOperator =
          (isDoLoop && leftZero && !rightZero) || (!isDoLoop && !leftZero && rightZero);
      switch (condition.getOperator().cmpValue()) {
          // use the inverse comparison because jumping means we execute the "else" part of the
          // condition
        case EQUAL:
          if (reverseOperator) {
            conditionOut.write(IFNE);
          } else {
            conditionOut.write(IFEQ);
          }
          break;
        case NOTEQUAL:
          if (reverseOperator) {
            conditionOut.write(IFEQ);
          } else {
            conditionOut.write(IFNE);
          }
          break;
        case GREATER:
          if (reverseOperator) {
            conditionOut.write(IFLE);
          } else {
            conditionOut.write(IFGT);
          }
          break;
        case GREATEREQ:
          if (reverseOperator) {
            conditionOut.write(IFLT);
          } else {
            conditionOut.write(IFGE);
          }
          break;
        case SMALLER:
          if (reverseOperator) {
            conditionOut.write(IFGE);
          } else {
            conditionOut.write(IFLT);
          }
          break;
        case SMALLEREQ:
          if (reverseOperator) {
            conditionOut.write(IFGT);
          } else {
            conditionOut.write(IFLE);
          }
          break;
      }
      return writeBranchOffset(out, branchBytes, isDoLoop, conditionOut);
    }
    switch (condition.getOperator().cmpValue()) {
      case EQUAL:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPEQ);
        } else {
          conditionOut.write(IF_ICMPNE);
        }
        break;
      case NOTEQUAL:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPNE);
        } else {
          conditionOut.write(IF_ICMPEQ);
        }
        break;
      case GREATER:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPGT);
        } else {
          conditionOut.write(IF_ICMPLE);
        }
        break;
      case GREATEREQ:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPGE);
        } else {
          conditionOut.write(IF_ICMPLT);
        }
        break;
      case SMALLER:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPLT);
        } else {
          conditionOut.write(IF_ICMPGE);
        }
        break;
      case SMALLEREQ:
        if (isDoLoop) {
          conditionOut.write(IF_ICMPLE);
        } else {
          conditionOut.write(IF_ICMPGT);
        }
        break;
    }
    return writeBranchOffset(out, branchBytes, isDoLoop, conditionOut);
  }

  public HasOutput writeBranchOffset(
      DynamicByteArray out,
      short branchBytes,
      boolean addConditionToBranchOffset,
      DynamicByteArray conditionOut) {
    if (addConditionToBranchOffset) {
      branchBytes -= conditionOut.getBytes().length;
      branchBytes++;
    }
    conditionOut.write(shortToByteArray(branchBytes));
    out.write(conditionOut.getBytes());
    return out;
  }

  public static boolean isZero(Object value) {
    return (value instanceof Integer && ((Integer) value).intValue() == 0);
  }
}
