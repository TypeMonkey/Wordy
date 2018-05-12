package wordy.logic.runtime.execution;

import java.util.Stack;

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
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.FileInstance;
import wordy.logic.runtime.RuntimeFile;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.types.Instance;
import wordy.logic.runtime.types.ValType;

public class GenVisitor implements NodeVisitor{
 
  private Stack<VariableMember> stack;
  
  private WordyRuntime runtime;
  private RuntimeTable table;
  
  private boolean pushVariable;
  
  public GenVisitor(RuntimeTable executor, WordyRuntime runtime) {
    this.stack = new Stack<>();
    this.table = executor;
    this.runtime = runtime;
  }
  
  public void visit(BinaryOpNode binaryOpNode) {
    System.out.println("----OPERATOR: "+binaryOpNode.getOperator()+" | LINE: "+binaryOpNode.tokens()[0].lineNumber());
    if (binaryOpNode.getOperator().equals(ReservedSymbols.EQUALS)) {
      pushVariable = true;
      binaryOpNode.getLeftOperand().accept(this);
      VariableMember settable = stack.pop();
      
      binaryOpNode.getRightOperand().accept(this);
      VariableMember value = stack.pop();
      
      if (value instanceof Constant) {
        Constant constant = (Constant) value;
        settable.setValue(constant.getValue(), constant.getType());
      }
      else {
        settable.setValue(value.getValue(), value.getType());
      }
    }
    else {
      binaryOpNode.getLeftOperand().accept(this);
      Constant leftConstant = null;
      if (stack.peek() instanceof Constant) {
        leftConstant = (Constant) stack.pop();
      }
      else {
        VariableMember left = stack.pop();
        leftConstant = (Constant) left.getValue();
      }
      
      binaryOpNode.getRightOperand().accept(this);
      Constant rightConstant = null;
      if (stack.peek() instanceof Constant) {
        rightConstant = (Constant) stack.pop();
      }
      else {
        VariableMember right = stack.pop();
        rightConstant = (Constant) right.getValue();
      }
      
      if (binaryOpNode.getOperator().equals(ReservedSymbols.EQUAL_EQ)) {
        stack.push(new Constant(ValType.BOOLEAN, leftConstant.getValue() == rightConstant.getValue()));
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
    String value = constNode.getValue();
    
    if (constNode.getValue().equals(ReservedSymbols.NULL)) {
      stack.push(new Constant(ValType.OBJECT, null));
    }
    else if (ReservedSymbols.isABooleanVal(constNode.getValue())) {
      stack.push(new Constant(ValType.BOOLEAN, Boolean.parseBoolean(constNode.getValue())));
    }
    else {
      try {
        stack.push(new Constant(ValType.INTEGER, Integer.parseInt(value)));
      } catch (NumberFormatException e) {
        stack.push(new Constant(ValType.DOUBLE, Double.parseDouble(constNode.getValue())));
      }
    }
  }

  public void visit(IdentifierNode identifierNode) {
    VariableMember member = table.findVariable(identifierNode.name());
    if (member == null) {
      FileInstance instance = runtime.findFile(identifierNode.name());
      if (instance == null) {
        String className = table.findBinaryName(identifierNode.name());
        if (className == null) {
          throw new RuntimeException("Can't find identifier '"+identifierNode.name()+"' at line "+
              identifierNode.tokens()[0].lineNumber());
        }
        else {
          //TODO: How do we wrap Java object to interact with the Wordy environment?
        }
      }
      
      member = new VariableMember(instance.getDefinition().getName(), true);
      member.setValue(instance, instance.getDefinition().getType());
      stack.push(member);
    }
    else {
      stack.push(member);
    }
  }

  public void visit(LiteralNode literalNode) {
    System.out.println("***PUSHING LITERAL: "+literalNode.getLiteralContent());
    stack.push(new Constant(ValType.STRING, literalNode.getLiteralContent()));
  }

  public void visit(MemberAccessNode memberAccessNode) {
    memberAccessNode.getCalle().accept(this);
    System.out.println("---IS FOR FUNC: "+memberAccessNode.isForFunction()+" | "+memberAccessNode.getMemberName());
    
    if (memberAccessNode.getCalle().nodeType() == NodeType.LITERAL ||
        memberAccessNode.getCalle().nodeType() == NodeType.CONSTANT) {
      throw new RuntimeException("Value types, like numbers and strings, don't have members at line "+
           memberAccessNode.tokens()[0].lineNumber());
    }
    
    
    if (memberAccessNode.isForFunction()) { 
        VariableMember member = stack.pop();
        stack.push(new Constant(member.getType(), member.getValue()));
    }
    else {
      VariableMember member = stack.pop();
      if (member.getValue() instanceof Instance) {
        Instance instance = (Instance) member.getValue();
        VariableMember instanceMem = instance.retrieveVariable(memberAccessNode.getMemberName().content());
        if (instanceMem == null) {
          throw new RuntimeException("Cannot find property '"+memberAccessNode.getMemberName().content()+"' "+
                                     "for instance of '"+instance.getDefinition().getName()+"' at line "+
                                      memberAccessNode.tokens()[0].lineNumber());
        }
        else {
          if (pushVariable) {
            stack.push(instanceMem);
          }
          else {
            stack.push(new Constant(instanceMem.getType(), instanceMem.getValue()));
          }
        }
      }
    }
  }

  public void visit(MethodCallNode callNode) {
    Token funcName = callNode.getName();
    
    //visit all arguments
    for(ASTNode nodeArg: callNode.arguments()) {
      nodeArg.accept(this);
    }
    
    //pop all nodes
    Constant [] args = new Constant[callNode.arguments().length];
    int index = args.length - 1;
    while (index >= 0) {
      VariableMember popped = stack.pop();
      if (popped instanceof Constant) {
        args[index] = (Constant) popped;
      }
      else {
        args[index] = new Constant(popped.getType(), popped.getValue());
      }
      index--;
    }
    
    if (callNode.getCallee().nodeType() == NodeType.IDENTIFIER) {
      //normal function call. Like : println()
      
      RuntimeTable frameExec = table.clone();
      GenVisitor frameVisitor = new GenVisitor(frameExec, runtime);
      
      Callable callable = table.findCallable(funcName.content(), args.length);
      if (callable == null) {
        Constant result = callable.call(frameVisitor, frameExec, args);
        stack.push(result);
      }
      else {
        
        Constant result = callable.call(frameVisitor, frameExec, args);   
        stack.push(result);
      }
    }
    else {
      if (!pushVariable) {
        pushVariable = false;
      }
      callNode.getCallee().accept(this);
      Constant member = (Constant) stack.pop();
      Instance instance = (Instance) member.getValue();
      
      System.out.println("----INSTANCE FUNC CALL "+member.getType().getTypeName());
      
      RuntimeTable frameExec = table.clone();
      GenVisitor frameVisitor = new GenVisitor(frameExec, runtime);
      
      Callable callable = instance.getDefinition().findFunction(funcName.content(), args.length);
      if (callable == null) {
        throw new RuntimeException("Can't find function '"+funcName.content()+"' at line "+
            funcName.lineNumber());
      }
      else {
        Constant result = callable.call(frameVisitor, frameExec, args);
        stack.push(result);
      }
    }
  }

  public void visit(UnaryNode unaryNode) {
   
  }

  public void resetStack() {
    stack.clear();
  }
  
  public VariableMember peekStack() {
    return stack.peek();
  }
}
