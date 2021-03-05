package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.generators.AssignmentGenerator;
import io.github.martinschneider.orzo.codegen.generators.BasicGenerator;
import io.github.martinschneider.orzo.codegen.generators.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.generators.IncrementGenerator;
import io.github.martinschneider.orzo.codegen.generators.InvokeGenerator;
import io.github.martinschneider.orzo.codegen.generators.LoadGenerator;
import io.github.martinschneider.orzo.codegen.generators.MethodCallGenerator;
import io.github.martinschneider.orzo.codegen.generators.PushGenerator;
import io.github.martinschneider.orzo.codegen.generators.StatementDelegator;
import io.github.martinschneider.orzo.codegen.generators.StoreGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.List;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public int currentIdx;
  public ConstantPool constPool;
  public Map<String, Method> methodMap;
  public StatementDelegator delegator;
  public AssignmentGenerator assignGen;
  public ExpressionGenerator exprGen;
  public IncrementGenerator incrGen;
  public MethodCallGenerator methodCallGen;
  public ConstantPoolProcessor constPoolProc;
  public BasicGenerator basicGen;
  public PushGenerator pushGen;
  public LoadGenerator loadGen;
  public StoreGenerator storeGen;
  public InvokeGenerator invokeGen;
  public OperandStack opStack;
  public CompilerErrors errors;

  public void init(CompilerErrors errors, int idx, List<Clazz> clazzes) {
    clazz = clazzes.get(idx);
    this.errors = errors;
    constPoolProc = new ConstantPoolProcessor(this);
    constPool = constPoolProc.processConstantPool(clazz);
    delegator = new StatementDelegator();
    exprGen = new ExpressionGenerator();
    incrGen = new IncrementGenerator(this);
    methodCallGen = new MethodCallGenerator(this);
    methodMap = new MethodProcessor().getMethodMap(clazz, clazzes);
    assignGen = new AssignmentGenerator(this);
    basicGen = new BasicGenerator(this);
    invokeGen = new InvokeGenerator(this);
    pushGen = new PushGenerator(this);
    loadGen = new LoadGenerator(this);
    storeGen = new StoreGenerator(this);
    opStack = new OperandStack();
    delegator.ctx = this;
    exprGen.ctx = this;
    delegator.init();
  }
}
