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
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CGContext {
  public Clazz clazz;
  public List<Clazz> allClazzes;
  // Same list CodeGenerator iterates with "for (int i = 0; i < clazzes.size(); i++)" (shared by
  // reference, not copied) — appending here mid-generation (e.g. a lambda's synthetic
  // implementing class) is picked up automatically since the loop bound is re-read every
  // iteration. Kept 1:1 and in lockstep with allOutputs by addSyntheticClazz().
  public List<Output> allOutputs;
  // Monotonically increasing, never reset per-class (ctx.init() doesn't touch it) so synthetic
  // class names (e.g. a lambda's functional-interface implementation) stay unique across the
  // whole compilation, not just within one class.
  public int syntheticClassCounter;
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
  public MemberProcessor memberProc;
  public GlobalIdentifierMap classIdMap;
  // Exception table entries: each int[] is {startPc, endPc, handlerPc, catchTypeIdx}
  public List<int[]> exceptionTable;
  // Maps handlerPc → JVM class name (e.g. "java/lang/Exception") for StackMapTable
  public Map<Integer, String> exceptionHandlerType;
  // finally blocks enclosing the statement being generated, innermost first: a return inside a
  // try block must run them first
  public java.util.Deque<List<io.github.martinschneider.orzo.parser.productions.Statement>>
      finallyStack = new java.util.ArrayDeque<>();

  public void init(CompilerErrors errors, int idx, List<Clazz> clazzes, List<Output> outputs) {
    exceptionTable = new ArrayList<>();
    exceptionHandlerType = new HashMap<>();
    clazz = clazzes.get(idx);
    allClazzes = clazzes;
    allOutputs = outputs;
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
    memberProc = new MemberProcessor(this);
    classIdMap = new GlobalIdentifierMap();
    classIdMap.variables = new IdentifierMap();
    delegator.ctx = this;
    exprGen.ctx = this;
    delegator.init();
  }

  // Registers a new class to be generated once the current class-list loop reaches it — used for
  // classes discovered mid-generation rather than present in the original input/clazzes list
  // (e.g. a lambda expression's synthetic implementation of its target functional interface).
  // CodeGenerator.generate()'s "for (int i = 0; i < clazzes.size(); i++)" picks this up on its
  // own since allClazzes/allOutputs are the SAME list objects it iterates over.
  public void addSyntheticClazz(Clazz clazz) {
    allClazzes.add(clazz);
    // A throwaway in-memory sink, same pattern the test harness uses - Output always buffers
    // everything written to it internally (getBytes()) regardless of what PrintStreams are
    // attached, so this PrintStream's own destination is irrelevant. Avoids needing a no-arg
    // Output constructor or an empty PrintStream array, both of which (separately) ran into a
    // pre-existing orzo codegen bug when this file self-compiles (an array-typed field's
    // null-check, or a zero-length array literal, each produced a malformed class file - see
    // docs/TODO.md).
    allOutputs.add(new Output(new PrintStream(new ByteArrayOutputStream())));
  }
}
