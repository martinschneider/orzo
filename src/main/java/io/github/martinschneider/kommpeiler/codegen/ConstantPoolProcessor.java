package io.github.martinschneider.kommpeiler.codegen;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Declaration;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;

public class ConstantPoolProcessor {
  private static ConstantPool processConstants(ConstantPool constantPool, Expression param) {
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

  public static ConstantPool processConstantPool(Clazz clazz) {
    ConstantPool constantPool = new ConstantPool();
    constantPool.addClass(clazz.getName().getValue().toString());
    constantPool.addClass("java/lang/Object");
    for (Method method : clazz.getBody()) {
      // add method name to constant pool
      constantPool.addUtf8(method.getName().getValue().toString());
      // add type descriptor to constant pool
      constantPool.addUtf8(method.getTypeDescr());
      // add constants from method body to constant pool
      for (Statement statement : method.getBody()) {
        if (statement instanceof MethodCall) {
          MethodCall methodCall = (MethodCall) statement;
          for (Expression param : methodCall.getParameters()) {
            constantPool = processConstants(constantPool, param);
          }
        } else if (statement instanceof Declaration) {
          Declaration decl = (Declaration) statement;
          Expression value = decl.getValue();
          if (value != null) {
            constantPool = processConstants(constantPool, value);
          }
        }
      }
      constantPool.addUtf8("Code");
    }
    return constantPool;
  }
}
