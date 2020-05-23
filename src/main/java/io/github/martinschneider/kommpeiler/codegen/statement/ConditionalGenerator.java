package io.github.martinschneider.kommpeiler.codegen.statement;

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
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LCMP;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Condition;
import java.math.BigInteger;

public class ConditionalGenerator {
  public CGContext context;

  public HasOutput generateCondition(
      DynamicByteArray out, VariableMap variables, Condition cond, short branchBytes) {
    return generateCondition(out, variables, cond, branchBytes, false);
  }

  public HasOutput generateCondition(
      DynamicByteArray out,
      VariableMap variables,
      Condition cond,
      short branchBytes,
      boolean isDoLoop) {
    DynamicByteArray condOut = new DynamicByteArray();
    // TODO: support other boolean conditions
    ExpressionResult left =
        context.exprGenerator.eval(condOut, variables, null, cond.getLeft(), false);
    ExpressionResult right =
        context.exprGenerator.eval(condOut, variables, left.getType(), cond.getRight(), false);
    if (left.getType().equals(INT)) {
      return generateIntCondition(condOut, out, cond, left, right, branchBytes, isDoLoop);
    } else if (left.getType().equals(LONG)) {
      return generateLongCondition(condOut, out, cond, left, right, branchBytes, isDoLoop);
    }
    return null;
  }

  private HasOutput generateLongCondition(
      DynamicByteArray condOut,
      DynamicByteArray out,
      Condition cond,
      ExpressionResult left,
      ExpressionResult right,
      short branchBytes,
      boolean isDoLoop) {
    condOut.write(LCMP);
    switch (cond.getOperator().cmpValue()) {
      case EQUAL:
        if (isDoLoop) {
          condOut.write(IFEQ);
        } else {
          condOut.write(IFNE);
        }
        break;
      case NOTEQUAL:
        if (isDoLoop) {
          condOut.write(IFNE);
        } else {
          condOut.write(IFEQ);
        }
        break;
      case GREATER:
        if (isDoLoop) {
          condOut.write(IFGT);
        } else {
          condOut.write(IFLE);
        }
        break;
      case GREATEREQ:
        if (isDoLoop) {
          condOut.write(IFGE);
        } else {
          condOut.write(IFLT);
        }
        break;
      case SMALLER:
        if (isDoLoop) {
          condOut.write(IFLT);
        } else {
          condOut.write(IFGE);
        }
        break;
      case SMALLEREQ:
        if (isDoLoop) {
          condOut.write(IFLE);
        } else {
          condOut.write(IFGT);
        }
        break;
    }
    return writeBranchOffset(out, branchBytes, isDoLoop, condOut);
  }

  private HasOutput generateIntCondition(
      DynamicByteArray condOut,
      DynamicByteArray out,
      Condition cond,
      ExpressionResult left,
      ExpressionResult right,
      short branchBytes,
      boolean isDoLoop) {
    boolean leftZero = isZero(left.getValue());
    boolean rightZero = isZero(right.getValue());
    if (leftZero ^ rightZero) {
      boolean reverseOp =
          (isDoLoop && leftZero && !rightZero) || (!isDoLoop && !leftZero && rightZero);
      switch (cond.getOperator().cmpValue()) {
          // use the inverse comparison because jumping means we execute the "else" part of the
          // condition
        case EQUAL:
          if (reverseOp) {
            condOut.write(IFNE);
          } else {
            condOut.write(IFEQ);
          }
          break;
        case NOTEQUAL:
          if (reverseOp) {
            condOut.write(IFEQ);
          } else {
            condOut.write(IFNE);
          }
          break;
        case GREATER:
          if (reverseOp) {
            condOut.write(IFLE);
          } else {
            condOut.write(IFGT);
          }
          break;
        case GREATEREQ:
          if (reverseOp) {
            condOut.write(IFLT);
          } else {
            condOut.write(IFGE);
          }
          break;
        case SMALLER:
          if (reverseOp) {
            condOut.write(IFGE);
          } else {
            condOut.write(IFLT);
          }
          break;
        case SMALLEREQ:
          if (reverseOp) {
            condOut.write(IFGT);
          } else {
            condOut.write(IFLE);
          }
          break;
      }
      return writeBranchOffset(out, branchBytes, isDoLoop, condOut);
    }
    switch (cond.getOperator().cmpValue()) {
      case EQUAL:
        if (isDoLoop) {
          condOut.write(IF_ICMPEQ);
        } else {
          condOut.write(IF_ICMPNE);
        }
        break;
      case NOTEQUAL:
        if (isDoLoop) {
          condOut.write(IF_ICMPNE);
        } else {
          condOut.write(IF_ICMPEQ);
        }
        break;
      case GREATER:
        if (isDoLoop) {
          condOut.write(IF_ICMPGT);
        } else {
          condOut.write(IF_ICMPLE);
        }
        break;
      case GREATEREQ:
        if (isDoLoop) {
          condOut.write(IF_ICMPGE);
        } else {
          condOut.write(IF_ICMPLT);
        }
        break;
      case SMALLER:
        if (isDoLoop) {
          condOut.write(IF_ICMPLT);
        } else {
          condOut.write(IF_ICMPGE);
        }
        break;
      case SMALLEREQ:
        if (isDoLoop) {
          condOut.write(IF_ICMPLE);
        } else {
          condOut.write(IF_ICMPGT);
        }
        break;
    }
    return writeBranchOffset(out, branchBytes, isDoLoop, condOut);
  }

  public HasOutput writeBranchOffset(
      DynamicByteArray out,
      short branchBytes,
      boolean addCondToBranchOffset,
      DynamicByteArray conditionOut) {
    if (addCondToBranchOffset) {
      branchBytes -= conditionOut.getBytes().length;
      branchBytes++;
    }
    conditionOut.write(shortToByteArray(branchBytes));
    out.write(conditionOut.getBytes());
    return out;
  }

  public static boolean isZero(Object value) {
    return (value instanceof BigInteger && value.equals(BigInteger.ZERO));
  }
}
