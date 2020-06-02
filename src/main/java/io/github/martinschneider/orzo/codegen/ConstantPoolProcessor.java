package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.lexer.tokens.DoubleNum;
import io.github.martinschneider.orzo.lexer.tokens.IntNum;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.ArrayInitialiser;
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

public class ConstantPoolProcessor {
  public ConstantPool processConstantPool(Clazz clazz) {
    ConstantPool constPool = new ConstantPool();
    constPool.addClass(clazz.getName().getValue().toString());
    constPool.addClass("java/lang/Object");
    for (Method method : clazz.getBody()) {
      // add method name to constant pool
      constPool.addUtf8(method.getName().getValue().toString());
      // add type descriptor to constant pool
      constPool.addUtf8(method.getTypeDescr());
      addMethodRef(
          constPool,
          clazz.getName().getValue().toString(),
          method.getName().getValue().toString(),
          method.getTypeDescr());
      // add constants from method body to constant pool
      for (Statement stmt : method.getBody()) {
        constPool = processStatement(constPool, stmt);
      }
      constPool.addUtf8("Code");
    }
    return constPool;
  }

  private ConstantPool processStatement(ConstantPool constPool, Statement stmt) {
    if (stmt instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) stmt;
      for (Expression param : methodCall.getParameters()) {
        constPool = processExpression(constPool, param);
      }
    } else if (stmt instanceof Declaration) {
      Declaration decl = (Declaration) stmt;
      Expression value = decl.getValue();
      if (value != null) {
        if (value instanceof ArrayInitialiser) {
          for (Expression arrInit : ((ArrayInitialiser) value).getValues()) {
            constPool = processExpression(constPool, decl.getType(), arrInit);
          }
        } else {
          constPool = processExpression(constPool, decl.getType(), value);
        }
      }
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      Expression value = assignment.getRight();
      if (value != null) {
        constPool = processExpression(constPool, value);
      }
    } else if (stmt instanceof ReturnStatement) {
      ReturnStatement ret = (ReturnStatement) stmt;
      constPool = processExpression(constPool, ret.getRetValue());
    } else if (stmt instanceof LoopStatement) {
      LoopStatement loopStatement = (LoopStatement) stmt;
      constPool = processExpression(constPool, loopStatement.getCondition().getLeft());
      constPool = processExpression(constPool, loopStatement.getCondition().getRight());
      for (Statement innerStmt : loopStatement.getBody()) {
        constPool = processStatement(constPool, innerStmt);
      }
    } else if (stmt instanceof IfStatement) {
      IfStatement ifStatement = (IfStatement) stmt;
      for (IfBlock ifBlock : ifStatement.getIfBlocks()) {
        for (Statement subStatement : ifBlock.getBody()) {
          constPool = processStatement(constPool, subStatement);
        }
        if (ifBlock.getCondition() != null) { // null for else blocks
          constPool = processExpression(constPool, null, ifBlock.getCondition().getLeft());
          constPool = processExpression(constPool, null, ifBlock.getCondition().getRight());
        }
      }
    }
    return constPool;
  }

  private ConstantPool processExpression(ConstantPool constPool, Expression param) {
    return processExpression(constPool, null, param);
  }

  private ConstantPool processExpression(ConstantPool constPool, String type, Expression param) {
    for (Token token : param.getInfix()) {
      if (token instanceof Str) {
        constPool.addString(token.getValue().toString());
      } else if (token instanceof IntNum) {
        long intValue = ((BigInteger) (token.getValue())).longValue();
        if (((IntNum) token).isLong()
            || (type != null && type.equals(LONG))
            || intValue < Integer.MIN_VALUE
            || intValue > Integer.MAX_VALUE) {
          constPool.addLong(intValue);
        } else if (intValue < -32768 || intValue >= 32768) {
          constPool.addInteger((int) intValue);
        }
      } else if (token instanceof DoubleNum) {
        BigDecimal doubleValue = ((BigDecimal) (token.getValue()));
        if (((DoubleNum) token).isFloat() || (type != null && type.equals(FLOAT))) {
          constPool.addFloat(doubleValue.floatValue());
        } else {
          constPool.addDouble(doubleValue.doubleValue());
        }
      }
    }
    return constPool;
  }

  public ConstantPool addMethodRef(
      ConstantPool constPool, String className, String methodName, String typeSignature) {
    constPool.addMethodRef(className, methodName, typeSignature);
    return constPool;
  }
}
