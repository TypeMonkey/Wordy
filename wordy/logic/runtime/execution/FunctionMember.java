package wordy.logic.runtime.execution;

import java.util.List;

import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Statement.StatementDescription;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.WhileLoopBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;
import wordy.logic.runtime.Constant;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.types.Instance;
import wordy.logic.runtime.types.ValType;

/**
 * Represents a callable function.
 * @author Jose Guaro
 *
 */
public class FunctionMember extends Callable{

  protected Statement [] statements;
  private boolean lastIf;
  
  /**
   * Constructs a FunctionMember
   * @param name - the name of this function
   * @param argumentAmnt - the amount of argument this function expects
   * @param statements - the Statements in this function
   */
  public FunctionMember(String name, int argumentAmnt, WordyRuntime runtime, Statement ... statements) {
    super(name, argumentAmnt, runtime);
    this.statements = statements;
  }
  
  /**
   * Invokes this function, passing in its required arguments
   */
  public Constant call(GenVisitor visitor, RuntimeTable table, Constant ... args) {    
    System.out.println("-----CALLED: "+getName());
    int argCnt = argAmnt;
    for(Statement statement: statements) {
      System.out.println("------NEXT STATEMENT------ "+statements.length+" || "+statements[0].getClass().getName());
      visitor.resetStack();
      if (statement.getDescription() == StatementDescription.BLOCK) {
        RuntimeTable blockExec = table.clone();
        BlockExecResult result = executeStatementBlock(new GenVisitor(blockExec, runtime), blockExec, (StatementBlock) statement);
        if (result.gotBreak()) {
          break;
        }
        else if (result.gotContinue()) {
          continue;
        }
        else if (result.gotReturn()) {
          return result.getReturnedObject();
        }
      }
      else if (statement.getDescription() == StatementDescription.VAR_DEC) {
        Variable variable = (Variable) statement;
        VariableMember variableMember = new VariableMember(variable.getName().content(), variable.isConstant());
        System.out.println("----PLACING VAR: "+variableMember.getName() + "|| " );
        if(table.placeVariable(0, variableMember)) {
          throw new RuntimeException("Duplicate variable '"+variableMember.getName()+"' at line "+
                                       variable.getName().lineNumber());
        }
        else {
          /*
           * What's with this?
           * 
           *  When we we're formatting the source code to fit our representation prior to runtime,
           *  we added the parameters of a function as a Variable/Statement in that function's
           *  body - while it technically isn't.
           *  
           *  Since the parameters are the first things we added to a function's Statement list,
           *  we can confidently use the first k-statements (with k being the amount of arguments)
           *  of a function as that functions arguments
           */
          if (argCnt > 0) {
            VariableMember value = args[argAmnt - argCnt];
            variableMember.setValue(value.getValue(), value.getType());
            argCnt--;
          }     
          else {
            if (variable.getExpression() != null) {
              variable.getExpression().accept(visitor);
              VariableMember value = visitor.peekStack();
              variableMember.setValue(value.getValue(), value.getType());
            }
          }
        }       
      }
      else if (statement.getDescription() == StatementDescription.RETURN) {
        if (statement.getExpression() == null) {
          //an "empty" return
          return Constant.VOID;
        }
        else {
          //actually returning a value
          System.out.println("---EXEC RETURN EXPR: "+statement.getExpression().getClass().getName());
          statement.getExpression().accept(visitor);
          VariableMember returned = visitor.peekStack();
          System.out.println("---EXEC RETURN EXPR Type: "+returned.getType().getTypeName());
          return new Constant(returned.getType(), returned.getValue());
        }
      }
      else {
        System.out.println("---EXEC NORM EXPR: "+statement.getExpression().getClass().getName());
        statement.getExpression().accept(visitor);
      }  
    }
    return Constant.VOID;
  }
  
  private BlockExecResult executeStatementBlock(GenVisitor visitor, RuntimeTable executor, StatementBlock block) {
    if (block.blockType() == BlockType.FOR) {
      return executeForLoop(visitor, executor, (ForLoopBlock) block);
    }
    else if (block.blockType() == BlockType.WHILE) {
      return executeWhile(visitor, executor, (WhileLoopBlock) block);
    }
    else if (block.blockType() == BlockType.IF) {
      return executeIf(visitor, executor, (IfBlock) block);
    }
    else {
      return executeBlock(visitor, executor, block.getStatements());
    }
  }
  
  private BlockExecResult executeIf(GenVisitor visitor, RuntimeTable executor, IfBlock ifBlock) {
    if(ifBlock.isElseIf()) {
      if(lastIf == false){
         if(ifBlock.getCondition() == null) {
           /*
            * Is just a pure else block. A.k.a: else{ }
            */
           return executeBlock(visitor, executor, ifBlock.getStatements());
         }
         else {
           /*
            * Is just an else if block. A.k.a: else if( /condtion/ ){ }
            */
           ifBlock.getExpression().accept(visitor);
           lastIf = (boolean) visitor.peekStack().getValue();
           if(lastIf) {
             return executeBlock(visitor, executor, ifBlock.getStatements());
           }
         }
       }
     }
     else {
       ifBlock.getExpression().accept(visitor);
       lastIf = (boolean) visitor.peekStack().getValue();;
       if(lastIf) {
         return executeBlock(visitor, executor, ifBlock.getStatements());
       }
     }
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  private BlockExecResult executeWhile(GenVisitor visitor, RuntimeTable executor, WhileLoopBlock whileLoop) {
    whileLoop.getCondition().getExpression().accept(visitor);
    boolean peeked = (boolean) visitor.peekStack().getValue();
    while(peeked) {      
      BlockExecResult result = executeBlock(visitor, executor, whileLoop.getStatements());
      if (result.gotBreak()) {
        break;
      }
      else if (result.gotContinue()) {
        continue;
      }
      else if (result.gotReturn()) {
        return result;
      }
      whileLoop.getExpression().accept(visitor);
      peeked = (boolean) visitor.peekStack().getValue();
    }        
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  private BlockExecResult executeForLoop(GenVisitor visitor, RuntimeTable executor, ForLoopBlock forLoop) {
    if (forLoop.getInitialization().getDescription() == StatementDescription.VAR_DEC) {
      Variable variable = (Variable) forLoop.getInitialization();
      VariableMember variableMember = new VariableMember(variable.getName().content(), variable.isConstant());
      executor.placeVariable(0,variableMember);
      variable.getExpression().accept(visitor);
    }
    
    forLoop.getCheckStatement().getExpression().accept(visitor);
    boolean peeked = (boolean) visitor.peekStack().getValue();
              
    while(peeked) {
      BlockExecResult result = executeBlock(visitor, executor, forLoop.getStatements());
      if (result.gotBreak()) {
        break;
      }
      else if (result.gotContinue()) {
        continue;
      }
      else if (result.gotReturn()) {
        return result;
      }
      forLoop.getChangeStatement().getExpression().accept(visitor);
      forLoop.getCheckStatement().getExpression().accept(visitor);
      peeked = (boolean) visitor.peekStack().getValue();
    }       
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);
  }
  
  private BlockExecResult executeBlock(GenVisitor visitor, RuntimeTable executor, List<Statement> statements) {
    for(Statement loopStatement: statements) {
      visitor.resetStack();
      if (loopStatement.getDescription() == StatementDescription.BREAK) {
        return new BlockExecResult(BlockExecResult.BREAK_ENCOUNTERED, null);
      }
      else if (loopStatement.getDescription() == StatementDescription.CONTINUE) {
        return new BlockExecResult(BlockExecResult.CONTINUE_ENCOUNTERED, null);
      }
      else if (loopStatement.getDescription() == StatementDescription.RETURN) {
        if (loopStatement.getExpression() != null) {
          loopStatement.getExpression().accept(visitor);
          VariableMember member = visitor.peekStack();
          return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, new Constant(member.getType(), member.getValue()));
        }
        else {
          return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, Constant.VOID);
        }
      }
      else if (loopStatement.getDescription() == StatementDescription.VAR_DEC) {
        Variable variable = (Variable) loopStatement;
        VariableMember variableMember = new VariableMember(variable.getName().content(), variable.isConstant());
        if(executor.placeVariable(0,variableMember)) {
          throw new RuntimeException("Duplicate variable '"+variableMember.getName()+"' at line "+
              variable.getExpression().tokens()[0].lineNumber());
        }
        else {
          if (variable.getExpression() != null) {
            variable.getExpression().accept(visitor);
            VariableMember member = visitor.peekStack();
            return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, new Constant(member.getType(), member.getValue()));
          }          
        }       
      }
      else if (loopStatement.getDescription() == StatementDescription.BLOCK) {
        StatementBlock block = (StatementBlock) loopStatement;
        executor = executor.clone();
        BlockExecResult result = executeStatementBlock(new GenVisitor(executor, runtime), executor, block);
        if (result.wasNormalEnd() == false) {
          return result;
        }
      }
      else {
        loopStatement.getExpression().accept(visitor);
      }
    }
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  /**
   * Checks if this FunctionMember is a class constructor
   * @return false if this FunctionMember isn't a constructor,
   *         true if this is a FunctionMember is a constructor
   */
  public boolean isAConstructor() {
    return false;
  }
  
  private static class BlockExecResult{
    
    static final int NORMAL_END = 3;
    static final int RETURN_ENCOUNTERED = 0;
    static final int BREAK_ENCOUNTERED = 1;
    static final int CONTINUE_ENCOUNTERED = 2;

    private final int encounter;
    private Constant got;
    
    public BlockExecResult(int encounter, Constant got) {
      this.encounter = encounter;
      this.got = got;
    }
    
    public Constant getReturnedObject() {
      return got;
    }
    
    public boolean gotReturn() {
      return encounter == RETURN_ENCOUNTERED;
    }
    
    public boolean gotBreak() {
      return encounter == BREAK_ENCOUNTERED;
    }
    
    public boolean gotContinue() {
      return encounter == CONTINUE_ENCOUNTERED;
    }
    
    public boolean wasNormalEnd() {
      return encounter == NORMAL_END;
    }
  }
}
