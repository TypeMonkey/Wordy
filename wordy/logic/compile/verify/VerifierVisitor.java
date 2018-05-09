package wordy.logic.compile.verify;

import java.util.Stack;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.BinaryOpNode;
import wordy.logic.compile.nodes.ConstantNode;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.nodes.LiteralNode;
import wordy.logic.compile.nodes.MemberAccessNode;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.nodes.UnaryNode;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Variable;

public class VerifierVisitor implements NodeVisitor{
  
  private SymbolTable table;
  private Stack<String> stack;
  private Token className;
  private Prior prior;
  private int argAmnt;

  private enum Prior{
    IDEN, FUNC, MEM_ACC;
  }
  
  public VerifierVisitor(SymbolTable table, Token className) {
    this.table = table;
    this.stack = new Stack<>();
    this.className = className;
  }

  @Override
  public void visit(BinaryOpNode node) {
    System.out.println("----VISITOR OP: "+node.getOperator());
    System.out.println("---VISITING LEFT "+node.getLeftOperand().getClass().getName());
    node.getLeftOperand().visit(this); 
    System.out.println("---VISITNG RIGHT "+node.getRightOperand().getClass().getName());
    node.getRightOperand().visit(this); 
    System.out.println("---OP DONE");
  }

  @Override
  public void visit(ConstantNode node) {
    /*
     * We don't care about constants.
     */
  }

  @Override
  public void visit(IdentifierNode node) {
    System.out.println("----VISITED IDENTIFIER: "+node.getTokenName().content()+" | "+prior);
    Token iden = node.getTokenName();
    if (prior == null || prior == Prior.MEM_ACC || prior == Prior.IDEN) {
      Variable variable = table.getVariable(iden.content());
      if (!iden.content().equals(ReservedSymbols.THIS)) {
        if (variable == null) {
          if (className == null) {
            if (table.getFileStruct(iden.content()) == null) {
              throw new ParseError("Can't find variable '"+iden.content()+"' ", iden.lineNumber());
            }
          }
          
          System.out.println("***CHECKING CLASS");
          ClassStruct struct = table.getClass(className.content());
          if (struct == null) {
            throw new ParseError("Can't find class '"+className.content()+"' ", iden.lineNumber());
          }
          
          variable = struct.getVariable(iden.content());
          if (variable == null) {
            throw new ParseError("Can't find variable '"+iden.content()+"' ", iden.lineNumber());
          }
        }    
        //System.out.println("*-*VAR FOUND");
      }
      else if (prior == Prior.FUNC) {
        Function function = table.getFunction(iden.content(), argAmnt);
        if (function == null) {
          System.out.println("---SYS FUNCS: "+table.getSystemFunctions());
          if (table.systemFunctionExists(iden.content(), argAmnt) == false) {
            if (className == null) {
              throw new ParseError("Can't find function '"+iden.content()+"' ", iden.lineNumber());
            }
            //System.out.println("*****CHECKING CLASS");
            ClassStruct struct = table.getClass(className.content());
            if (struct == null) {
              throw new ParseError("Can't find class '"+className.content()+"' ", iden.lineNumber());
            }
            
            function = struct.getFunction(iden.content(),argAmnt);
            if (function == null) {
              throw new ParseError("Can't find function '"+iden.content()+"' ", iden.lineNumber());
            }
          }
        }
      }
      //System.out.println("*-*FUNC FOUND");
    }
    prior = Prior.IDEN;
    stack.push(node.getTokenName().content());
  }

  @Override
  public void visit(LiteralNode node) {
    /*
     * We don't care about literals
     */
  }

  @Override
  public void visit(MemberAccessNode node) {
    System.out.println("---MEMBER ACCESS: "+node.getMemberName()+" | "+prior);
    prior = Prior.MEM_ACC;
    node.getCalle().visit(this);
    
    /**
     * Ignore this as we can't really access object members
     * because we don't know types pre-runtime, unless the member is
     * "this"
     */
  }

  @Override
  public void visit(MethodCallNode node) {
    System.out.println("----VISITED FUNCTION CALL "+node.getCallee().getClass().getName()+" | "+prior);
    prior = Prior.FUNC;
    argAmnt = node.arguments().length;
    node.getCallee().visit(this);
    
    for(int i = 0; i < node.arguments().length; i++) {
      System.out.println("---ARG "+i+" ----");
      node.arguments()[i].visit(this);
      System.out.println("---ARG "+i+" END----");
    }
  }

  @Override
  public void visit(UnaryNode node) {
    System.out.println("---VISITVED UNARY: "+node.tokens()[0]);
    /*
     * We don't care about unary ops
     */
  }
}
