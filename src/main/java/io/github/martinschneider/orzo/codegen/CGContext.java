package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.codegen.generators.AssignmentGenerator;
import io.github.martinschneider.orzo.codegen.generators.BasicGenerator;
import io.github.martinschneider.orzo.codegen.generators.ExpressionGenerator;
import io.github.martinschneider.orzo.codegen.generators.IncrementGenerator;
import io.github.martinschneider.orzo.codegen.generators.InvokeGenerator;
import io.github.martinschneider.orzo.codegen.generators.LoadGenerator;
import io.github.martinschneider.orzo.codegen.generators.MethodCallGenerator;
import io.github.martinschneider.orzo.codegen.generators.MethodGenerator;
import io.github.martinschneider.orzo.codegen.generators.PushGenerator;
import io.github.martinschneider.orzo.codegen.generators.StatementDelegator;
import io.github.martinschneider.orzo.codegen.generators.StoreGenerator;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.IdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.MemberProcessor;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import java.util.List;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public List<Clazz> allClazzes;
  public ConstantPool constPool;
  public Map<String, Method> methodMap;
  public Map<String, FieldProcessor.StaticField> staticFieldMap;
  public StatementDelegator delegator;
  public AssignmentGenerator assignGen;
  public ExpressionGenerator exprGen;
  public IncrementGenerator incrGen;
  public MethodGenerator methodGen;
  public MethodCallGenerator methodCallGen;
  public ConstantPoolProcessor constPoolProc;
  public BasicGenerator basicGen;
  public PushGenerator pushGen;
  public LoadGenerator loadGen;
  public StoreGenerator storeGen;
  public InvokeGenerator invokeGen;
  public OperandStack opStack;
  public CompilerErrors errors;
  public CodeGenerator codeGen;
  public MemberProcessor memberProc;
  public GlobalIdentifierMap classIdMap;

  public void init(CompilerErrors errors, CodeGenerator codeGen, int idx, List<Clazz> clazzes) {
    clazz = clazzes.get(idx);
    allClazzes = clazzes;
    this.errors = errors;
    constPoolProc = new ConstantPoolProcessor(this);
    constPool = constPoolProc.processConstantPool(clazz);
    delegator = new StatementDelegator();
    exprGen = new ExpressionGenerator();
    incrGen = new IncrementGenerator(this);
    methodGen = new MethodGenerator(this);
    methodCallGen = new MethodCallGenerator(this);
    methodMap = new MethodProcessor().getMethodMap(clazz, clazzes);
    staticFieldMap = new FieldProcessor().getStaticFieldMap(clazz, clazzes);
    assignGen = new AssignmentGenerator(this);
    basicGen = new BasicGenerator(this);
    invokeGen = new InvokeGenerator(this);
    pushGen = new PushGenerator(this);
    loadGen = new LoadGenerator(this);
    storeGen = new StoreGenerator(this);
    opStack = new OperandStack();
    this.codeGen = codeGen;
    memberProc = new MemberProcessor(this);
    classIdMap = new GlobalIdentifierMap();
    classIdMap.variables = new IdentifierMap();
    delegator.ctx = this;
    exprGen.ctx = this;
    delegator.init();
  }
}
