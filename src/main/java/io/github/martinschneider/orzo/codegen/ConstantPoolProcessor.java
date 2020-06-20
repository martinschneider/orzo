package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class ConstantPoolProcessor {
  public ConstantPool processConstantPool(Clazz clazz) {
    ConstantPool constPool = new ConstantPool();
    constPool.addClass(clazz.name.val.toString());
    constPool.addClass("java/lang/Object");
    for (Method method : clazz.body) {
      // add method name to constant pool
      constPool.addUtf8(method.name.val.toString());
      // add type descriptor to constant pool
      constPool.addUtf8(TypeUtils.methodDescr(method));
      constPool.addMethodRef(
          clazz.name.val.toString(), method.name.val.toString(), TypeUtils.methodDescr(method));
      // add constants from method body to constant pool
      for (Statement stmt : method.body) {
        constPool = processStatement(constPool, stmt);
      }
      constPool.addUtf8("Code");
    }
    return constPool;
  }

  private ConstantPool processStatement(ConstantPool constPool, Statement stmt) {
    if (stmt instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) stmt;
      for (Expression param : methodCall.params) {
        constPool = processExpression(constPool, param);
      }
    } else if (stmt instanceof Declaration) {
      Declaration decl = (Declaration) stmt;
      Expression val = decl.val;
      if (val != null) {
        if (val instanceof ArrayInit) {
          for (List<Expression> exprs : ((ArrayInit) val).vals) {
            for (Expression arrInit : exprs) {
              constPool = processExpression(constPool, decl.type, arrInit);
            }
          }
        } else {
          constPool = processExpression(constPool, decl.type, val);
        }
      }
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      Expression val = assignment.right;
      if (val != null) {
        constPool = processExpression(constPool, val);
      }
    } else if (stmt instanceof ReturnStatement) {
      ReturnStatement ret = (ReturnStatement) stmt;
      constPool = processExpression(constPool, ret.retValue);
    } else if (stmt instanceof LoopStatement) {
      LoopStatement loopStatement = (LoopStatement) stmt;
      constPool = processExpression(constPool, loopStatement.cond.left);
      constPool = processExpression(constPool, loopStatement.cond.right);
      for (Statement innerStmt : loopStatement.body) {
        constPool = processStatement(constPool, innerStmt);
      }
    } else if (stmt instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) stmt;
      for (IfBlock ifBlock : ifStatement.ifBlks) {
        for (Statement subStatement : ifBlock.body) {
          constPool = processStatement(constPool, subStatement);
        }
        if (ifBlock.cond != null) { // null for else blocks
          constPool = processExpression(constPool, null, ifBlock.cond.left);
          constPool = processExpression(constPool, null, ifBlock.cond.right);
        }
      }
    }
    return constPool;
  }

  private ConstantPool processExpression(ConstantPool constPool, Expression param) {
    return processExpression(constPool, null, param);
  }

  private ConstantPool processExpression(ConstantPool constPool, String type, Expression param) {
    for (Token token : param.tokens) {
      if (token instanceof Str) {
        constPool.addString(token.val.toString());
      } else if (token instanceof IntLiteral) {
        long intValue = ((BigInteger) (token.val)).longValue();
        if (((IntLiteral) token).isLong
            || (type != null && type.equals(LONG))
            || intValue < Integer.MIN_VALUE
            || intValue > Integer.MAX_VALUE) {
          constPool.addLong(intValue);
        } else if (intValue < -32768 || intValue >= 32768) {
          constPool.addInteger((int) intValue);
        }
      } else if (token instanceof FPLiteral) {
        BigDecimal doubleValue = ((BigDecimal) (token.val));
        if (((FPLiteral) token).isFloat || (type != null && type.equals(FLOAT))) {
          constPool.addFloat(doubleValue.floatValue());
        } else {
          constPool.addDouble(doubleValue.doubleValue());
        }
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        for (Expression subParam : methodCall.params) {
          constPool = processExpression(constPool, subParam);
        }
      }
    }
    return constPool;
  }
}
