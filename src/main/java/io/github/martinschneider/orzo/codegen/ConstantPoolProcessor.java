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
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class ConstantPoolProcessor {

  private CGContext ctx;

  public ConstantPoolProcessor(CGContext ctx) {
    this.ctx = ctx;
  }

  public ConstantPool processConstantPool(Clazz clazz) {
    ConstantPool constPool = new ConstantPool(ctx);
    constPool.addClass(clazz.fqn('/'));
    constPool.addClass("java/lang/Object");
    for (String interfaceName : clazz.interfaces) {
      // TODO: support interfaces from other packages
      constPool.addClass((clazz.packageName + "." + interfaceName).replace('.', '/'));
    }
    for (Method method : clazz.methods) {
      if (clazz.isInterface) {
        constPool.addUtf8(method.name.val.toString());
        constPool.addUtf8(TypeUtils.methodDescr(method));
      } else {
        constPool.addMethodRef(
            clazz.fqn('/'), method.name.val.toString(), TypeUtils.methodDescr(method));
        for (Statement stmt : method.body) {
          constPool = processStatement(constPool, stmt);
        }
        constPool.addUtf8("Code");
      }
    }
    for (ParallelDeclaration pDecl : clazz.fields) {
      for (Declaration decl : pDecl.declarations) {
        constPool.addFieldRef(
            clazz.fqn('/'), decl.name.id().toString(), TypeUtils.descr(decl.type, decl.arrDim));
      }
    }
    return constPool;
  }

  private ConstantPool processStatement(ConstantPool constPool, Statement stmt) {
    if (stmt instanceof MethodCall) {
      MethodCall methodCall = (MethodCall) stmt;
      for (Expression param : methodCall.params) {
        constPool = processExpression(constPool, param);
      }
    } else if (stmt instanceof ParallelDeclaration) {
      ParallelDeclaration pDecl = (ParallelDeclaration) stmt;
      for (Declaration decl : pDecl.declarations) {
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
      }
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      for (Expression val : assignment.right) {
        if (val != null) {
          constPool = processExpression(constPool, val);
        }
      }
    } else if (stmt instanceof ReturnStatement) {
      ReturnStatement ret = (ReturnStatement) stmt;
      constPool = processExpression(constPool, ret.retValue);
    } else if (stmt instanceof LoopStatement) {
      LoopStatement loopStatement = (LoopStatement) stmt;
      constPool = processExpression(constPool, loopStatement.cond);
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
          constPool = processExpression(constPool, null, ifBlock.cond);
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
