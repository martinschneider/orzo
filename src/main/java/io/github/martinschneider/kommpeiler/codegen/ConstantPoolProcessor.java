package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.ConditionalStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Return;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;

public class ConstantPoolProcessor {
  public ConstantPool processConstantPool(Clazz clazz) {
    ConstantPool constantPool = new ConstantPool();
    constantPool.addClass(clazz.getName().getValue().toString());
    constantPool.addClass("java/lang/Object");
    for (Method method : clazz.getBody()) {
      // add method name to constant pool
      constantPool.addUtf8(method.getName().getValue().toString());
      // add type descriptor to constant pool
      constantPool.addUtf8(method.getTypeDescr());
      addMethodRef(
          constantPool,
          clazz.getName().getValue().toString(),
          method.getName().getValue().toString(),
          method.getTypeDescr());
      // add constants from method body to constant pool
      for (Statement statement : method.getBody()) {
        constantPool = processStatement(constantPool, statement);
      }
      constantPool.addUtf8("Code");
    }
    return constantPool;
  }

  private ConstantPool processStatement(ConstantPool constantPool, Statement statement) {
    if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      for (Expression param : methodCall.getParameters()) {
        constantPool = processExpression(constantPool, param);
      }
    } else if (statement instanceof Declaration) {
      Declaration decl = (Declaration) statement;
      Expression value = decl.getValue();
      if (value != null) {
        constantPool = processExpression(constantPool, value);
      }
    } else if (statement instanceof Assignment) {
      Assignment assignment = (Assignment) statement;
      Expression value = assignment.getRight();
      if (value != null) {
        constantPool = processExpression(constantPool, value);
      }
    } else if (statement instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) statement;
      for (Expression param : methodCall.getParameters()) {
        constantPool = processExpression(constantPool, param);
      }
    } else if (statement instanceof Return) {
      Return ret = (Return) statement;
      constantPool = processExpression(constantPool, ret.getRetValue());
    } else if (statement instanceof ConditionalStatement) {
      ConditionalStatement conditional = (ConditionalStatement) statement;
      constantPool = processExpression(constantPool, conditional.getCondition().getLeft());
      constantPool = processExpression(constantPool, conditional.getCondition().getRight());
      for (Statement stmt : conditional.getBody()) {
        constantPool = processStatement(constantPool, stmt);
      }
    }
    return constantPool;
  }

  private ConstantPool processExpression(ConstantPool constantPool, Expression param) {
    for (Token token : param.getInfix()) {
      if (token instanceof Str) {
        constantPool.addString(token.getValue().toString());
      } else if (token instanceof IntNum) {
        int intValue = (Integer) (token.getValue());
        if (intValue < -32768 || intValue >= 32768) {
          constantPool.addInteger(intValue);
        }
      }
    }
    return constantPool;
  }

  public ConstantPool addMethodRef(
      ConstantPool constantPool, String className, String methodName, String typeSignature) {
    constantPool.addMethodRef(className, methodName, typeSignature);
    return constantPool;
  }
}
