package wordy.logic.runtime.execution;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Stack;

import wordy.logic.common.JavaFunctionKey;
import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.nodes.BinaryOpNode;
import wordy.logic.compile.nodes.ConstantNode;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.nodes.LiteralNode;
import wordy.logic.compile.nodes.MemberAccessNode;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.nodes.UnaryNode;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.nodes.ASTNode.NodeType;
import wordy.logic.runtime.RuntimeFile;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.components.StackComponent;

public class GenVisitor implements NodeVisitor{
 
  private Stack<StackComponent> stack;
  
  private WordyRuntime runtime;
  private RuntimeTable table;
  private FileInstance currentFile;
  
  //if the next content is a variable, then push the actual variable,
  //not the value it holds
  private boolean pushVariable;
  
  public GenVisitor(RuntimeTable executor, FileInstance currentFile, WordyRuntime runtime) {
    this.stack = new Stack<>();
    this.table = executor;
    this.currentFile = currentFile;
    this.runtime = runtime;
  }
  
  public void visit(BinaryOpNode binaryOpNode) {
    //System.out.println("----OPERATOR: "+binaryOpNode.getOperator()+" | LINE: "+binaryOpNode.getRightOperand());
    if (binaryOpNode.getOperator().equals(ReservedSymbols.EQUALS)) {
      pushVariable = true;
      binaryOpNode.getLeftOperand().accept(this);
      VariableMember settable = (VariableMember) stack.pop();
      
      binaryOpNode.getRightOperand().accept(this);
      StackComponent value = stack.pop();
      
      if (value instanceof Instance) {
        settable.setValue((Instance) value);
      }
      else {
        VariableMember leftVal = (VariableMember) value;
        settable.setValue(leftVal.getValue());
      }
      
      //System.out.println("--> EQUAL AFTER: "+settable.getValue().getClass());
    }
    else {
      binaryOpNode.getLeftOperand().accept(this);
      JavaInstance leftConstant = null;
      if (stack.peek() instanceof JavaInstance) {
        leftConstant = (JavaInstance) stack.pop();
      }
      else {
        VariableMember left = (VariableMember) stack.pop();
        leftConstant = (JavaInstance) left.getValue();
        //System.out.println("---LEFT: "+leftConstant);
      }
            
      binaryOpNode.getRightOperand().accept(this);
      JavaInstance rightConstant = null;
      if (stack.peek() instanceof JavaInstance) {
        rightConstant = (JavaInstance) stack.pop();
      }
      else {
        VariableMember right = (VariableMember) stack.pop();
        rightConstant = (JavaInstance) right.getValue();
      }
      
      if (binaryOpNode.getOperator().equals(ReservedSymbols.EQUAL_EQ)) {
        stack.push(JavaInstance.wrapInstance(leftConstant.equality(rightConstant)));
      }
      else if (ReservedSymbols.isAComparisonOp(binaryOpNode.getOperator())) {
        stack.push(Operator.arithemticComparison(leftConstant, rightConstant, binaryOpNode.tokens()[0]));
      }
      else if (ReservedSymbols.isABooleanOperator(binaryOpNode.getOperator())) {
        stack.push(Operator.booleanOperations(leftConstant, rightConstant, binaryOpNode.tokens()[0]));
      }
      else {
        stack.push(Operator.simpleArithmetic(leftConstant, rightConstant, binaryOpNode.tokens()[0]));
      }
    }
  }

  public void visit(ConstantNode constNode) {
    //System.out.println("***VISITED CONSTANT*****");
    String value = constNode.getValue();
    
    if (constNode.getValue().equals(ReservedSymbols.NULL)) {
      stack.push(JavaInstance.getNullRep());
    }
    else if (ReservedSymbols.isABooleanVal(constNode.getValue())) {
      stack.push(JavaInstance.wrapInstance(new Boolean(value)));
    }
    else {
      try {
        stack.push(JavaInstance.wrapInstance(new Integer(value)));
      } catch (NumberFormatException e) {
        try {
          stack.push(JavaInstance.wrapInstance(new Long(value)));
        } catch (NumberFormatException e2) {
          stack.push(JavaInstance.wrapInstance(new Double(value)));
        }
      }
    }
  }

  public void visit(IdentifierNode identifierNode) {
    //System.out.println("----IDENT: "+identifierNode.name());
    VariableMember member = table.findVariable(identifierNode.name());
    if (member == null) {
      FileInstance instance = runtime.findFile(identifierNode.name());
      if (instance == null) {
        String className = table.findBinaryName(identifierNode.name());
        //System.out.println("--- FOUND CLASS: "+className);
        if (className == null) {
          throw new RuntimeException("Can't find identifier '"+identifierNode.name()+"' at line "+
              identifierNode.tokens()[0].lineNumber());
        }
        else {
          try {
            //System.out.println("--- STAT INSTANCE: |"+className);
            JavaInstance staticRef = JavaInstance.createStaticInstance(Class.forName(className));
            //System.out.println("---pushed--- "+(staticRef == null));
            stack.push(staticRef);
          } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot find the class '"+className+"'");
          }
        }
      }
      else {
        stack.push(instance);
      }
    }
    else {
      //System.out.println("---FOUND VAR! "+identifierNode.name()+" | "+member.getType());
      //System.out.println(member.getValue().getClass());
      stack.push(member);
      //System.out.println("---stack: "+stack.size());
    }
  }

  public void visit(LiteralNode literalNode) {
   // System.out.println("***PUSHING LITERAL: "+literalNode.getLiteralContent());
    stack.push(JavaInstance.wrapInstance(literalNode.getLiteralContent()));
  }

  public void visit(MemberAccessNode memberAccessNode) {
    memberAccessNode.getCalle().accept(this);
    //System.out.println("---IS FOR FUNC: "+memberAccessNode.isForFunction()+" | "+memberAccessNode.getMemberName());
    
    if (memberAccessNode.isForFunction()) { 
        //Pass along
        StackComponent component = stack.pop();
        //System.out.println("---PUSHED - func: "+component.getClass());
        stack.push(component);
    }
    else {
      StackComponent member = stack.pop();
      if (member instanceof Instance) {
        Instance instance = (Instance) member;
        VariableMember instanceMem = instance.retrieveVariable(memberAccessNode.getMemberName().content());
        if (instanceMem == null) {
          throw new RuntimeException("Cannot find property '"+memberAccessNode.getMemberName().content()+"' "+
                                     "for instance of '"+instance.getDefinition().getName()+"' at line "+
                                      memberAccessNode.tokens()[0].lineNumber());
        }
        else {
          //System.out.println("---PUSHED: "+instanceMem);
          if (pushVariable) {
            stack.push(instanceMem);
          }
          else {
            stack.push(instanceMem.getValue());
          }
        }
      }
      else {
        VariableMember variable = (VariableMember) member;
        //System.out.println("---VARIABLE: "+variable.getName()+ " | "+variable.getClass()+" | "+variable.getType());
        VariableMember instanceMem = variable.getValue().retrieveVariable(memberAccessNode.getMemberName().content());
        //System.out.println("----Var member: "+instanceMem+" | "+instanceMem.getType());
        if (instanceMem == null) {
          throw new RuntimeException("Cannot find property '"+memberAccessNode.getMemberName().content()+"' "+
              "for instance of '"+variable.getType().getName()+"' at line "+
               memberAccessNode.tokens()[0].lineNumber());
        }
        //System.out.println("---INSTANCE: "+(instanceMem.getValue() == null));
        stack.push(instanceMem.getValue());
      }
    }
  }

  public void visit(MethodCallNode callNode) {
    Token funcName = callNode.getName();
    
    //visit all arguments
    for(ASTNode nodeArg: callNode.arguments()) {
      //System.out.println("****ARGS "+stack.size()+" | "+nodeArg.getClass());
      nodeArg.accept(this);
      //System.out.println("***ARGS DONE "+stack.size()+" | "+stack.peek());
    }
    
    //pop all nodes
    Instance [] args = new Instance[callNode.arguments().length];
    int index = args.length - 1;
    while (index >= 0) {
      StackComponent popped = stack.pop();
      //System.out.println(" -- call: "+(popped == null)+" | "+currentFile.getName());
      if (popped instanceof Instance) {
        args[index] = (Instance) popped;
      }
      else {
        VariableMember poppedVar = (VariableMember) popped;
        //System.out.println("---poppedVar "+ (poppedVar == null));
        args[index] = poppedVar.getValue();
      }
      index--;
    }
    
    if (callNode.getCallee().nodeType() == NodeType.IDENTIFIER) {
      //normal function call. Like : println()
      
      //System.out.println("---CALLING: "+funcName.content());
      RuntimeTable frameExec = table.clone(false);
      frameExec.clearLocalVars();
      //System.out.println("---ABOUT TO CALL");
      GenVisitor frameVisitor = new GenVisitor(frameExec, currentFile, runtime);
      
      Callable callable = table.findCallable(funcName.content(), args.length);
      if (callable == null) {
        callable = table.findCallable(JavaFunctionKey.spawnKey(funcName.content(), args));
        System.out.println("---TRYING: "+JavaFunctionKey.spawnKey(funcName.content(), args));
        if (callable == null) {
          throw new RuntimeException("Can't find function '"+funcName.content()+"' at line "+
              funcName.lineNumber());
        }
        else {
          Instance result = callable.call(frameVisitor,frameExec, args);   
          stack.push(result);
        }
      }
      else {    
        //System.out.println("---FUNC ARGS: "+args.length+" | "+callable.getName()+" | "+callNode.getName().lineNumber());
        Instance result = callable.call(frameVisitor,frameExec, args);   
        stack.push(result);
        //System.out.println("-----BACK FROM CAL TO: "+funcName.content()+" | ");
      }
    }
    else {
      if (!pushVariable) {
        pushVariable = false;
      }
      callNode.getCallee().accept(this);
      
      Instance instance = null;
      if (stack.peek() instanceof VariableMember) {
        VariableMember member = (VariableMember) stack.pop();
        instance = member.getValue();
      }
      else {
        instance = (Instance) stack.pop();
      }
      
      //System.out.println("----INSTANCE FUNC CALL "+instance.getClass()+" | "+args.length);
      
      RuntimeTable frameExec = table.clone(false);
      frameExec.clearLocalVars();
      GenVisitor frameVisitor = new GenVisitor(frameExec, currentFile, runtime);
      
      Callable callable = instance.getDefinition().findFunction(funcName.content(), args.length);
      if (callable instanceof JavaCallable) {
        JavaCallable javaCallable = (JavaCallable) callable;
        Instance result = null;
        if (javaCallable.isStatic() || javaCallable.isAConstructor()) {
          result = javaCallable.call(frameVisitor, frameExec, args);
        }
        else {
          Instance [] javaArgs = new Instance[args.length + 1]; //put first element as the java Instance
          javaArgs[0] = instance;
          System.arraycopy(args, 0, javaArgs, 1, args.length);
          //System.out.println("----CALLING: "+javaCallable.getName());
          result = javaCallable.call(frameVisitor, frameExec, javaArgs);
          //System.out.println("---AFTER CALL: "+javaCallable.getName());
        }
        stack.push(result);
      }
      else {
        if (callable == null) {
          throw new RuntimeException("Can't find function '"+funcName.content()+"' at line "+
              funcName.lineNumber());
        }
        else {
          Instance result = null;
          if (instance instanceof FileInstance) {
            result = callable.call(frameVisitor, frameExec, args);
          }
          else {
            result = callable.call(frameVisitor, frameExec, args);
          }
          stack.push(result);
        }
      }
    }
  }

  public void visit(UnaryNode unaryNode) {
    unaryNode.getExpr().accept(this);
    StackComponent value = stack.pop();
    if (value instanceof VariableMember) {
      VariableMember variableMember = (VariableMember) value;
      stack.push(Operator.performUnaryOperation((JavaInstance) variableMember.getValue(), 
                                                 unaryNode.tokens()[0]));
    }
    else {
      stack.push(Operator.performUnaryOperation((JavaInstance) value, 
                                                unaryNode.tokens()[0]));
    }
  }

  public void resetStack() {
    stack.clear();
  }
  
  public StackComponent peekStack() {
    return stack.peek();
  }
}
