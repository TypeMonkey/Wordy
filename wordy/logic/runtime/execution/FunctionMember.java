package wordy.logic.runtime.execution;

import java.util.Arrays;
import java.util.List;

import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Statement.StatementDescription;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.WhileLoopBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;
import wordy.logic.runtime.RuntimeFile;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.components.StackComponent;

/**
 * Represents a callable function.
 * @author Jose Guaro
 *
 */
public class FunctionMember extends Callable{

  protected Statement [] statements;
  protected FileInstance currentFile;
  private boolean lastIf;
  
  /**
   * Constructs a FunctionMember
   * @param name - the name of this function
   * @param argumentAmnt - the amount of argument this function expects
   * @param statements - the Statements in this function
   */
  public FunctionMember(String name, 
                        int argumentAmnt, 
                        WordyRuntime runtime, 
                        FileInstance currentFile, 
                        Statement ... statements) {
    super(name, argumentAmnt, runtime);
    this.statements = statements;
    this.currentFile = currentFile;
  }
  
  /**
   * Invokes this function, passing in its required arguments
   */
  public Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args) {    
    //System.out.println("*****---CALLED: "+getName()+" | ");
    table.addFuncMap(currentFile.getDefinition().getFunctions());
    int argCnt = argAmnt;
    for(Statement statement: statements) {
      //System.out.println("------NEXT STATEMENT------ || "+currentFile.getName());
      visitor.resetStack();
      if (statement.getDescription() == StatementDescription.BLOCK) {
        RuntimeTable blockExec = table.clone(true);
        BlockExecResult result = executeStatementBlock(new GenVisitor(blockExec, currentFile, runtime), 
                                                       blockExec, 
                                                       (StatementBlock) statement);
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
        //System.out.println("----PLACING VAR: "+variableMember.getName() + "|| " );
        if(table.placeLocalVar(variableMember)) {
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
            Instance value = args[argAmnt - argCnt];
            //System.out.println("-----FUNC PARAM: "+value+" | "+variableMember.getName());
            variableMember.setValue(value);
            argCnt--;
          }     
          else {
            if (variable.getExpression() != null) {
              variable.getExpression().accept(visitor);
              StackComponent value = visitor.peekStack();
              if (value instanceof Instance) {
                variableMember.setValue((Instance) value);
              }
              else {
                VariableMember peekedVar = (VariableMember) value;
                variableMember.setValue(peekedVar.getValue());
              }
              //System.out.println("----GOT: "+variableMember.getValue().getClass());
            }
          }
        }       
      }
      else if (statement.getDescription() == StatementDescription.RETURN) {
        if (statement.getExpression() == null) {
          //an "empty" return
          return null;
        }
        else {
          //actually returning a value
          //System.out.println("---EXEC RETURN EXPR: "+statement.getExpression().getClass().getName());
          statement.getExpression().accept(visitor);
          StackComponent checkPeeked = visitor.peekStack();
          if (checkPeeked instanceof Instance) {
            return (Instance) checkPeeked;
          }
          else {
            VariableMember peekedVar = (VariableMember) checkPeeked;
            return peekedVar.getValue();
          }      
        }
      }
      else {
        //System.out.println("---EXEC NORM EXPR: "+statement.getExpression().getClass().getName());
        statement.getExpression().accept(visitor);
      }  
    }
    
    //System.out.println("*****^^ FINISH CALL "+name+" | ");
    return null;
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
           StackComponent checkPeeked = visitor.peekStack();
           if (checkPeeked instanceof JavaInstance) {
             lastIf = (boolean) ((JavaInstance) checkPeeked).getInstance();
           }
           else {
             VariableMember peekedVar = (VariableMember) checkPeeked;
             JavaInstance instance = (JavaInstance) peekedVar.getValue();
             lastIf = (boolean) instance.getInstance();
           }      
           if(lastIf) {
             return executeBlock(visitor, executor, ifBlock.getStatements());
           }
         }
       }
     }
     else {
       ifBlock.getExpression().accept(visitor);
       StackComponent checkPeeked = visitor.peekStack();
       if (checkPeeked instanceof JavaInstance) {
         lastIf = (boolean) ((JavaInstance) checkPeeked).getInstance();
       }
       else {
         VariableMember peekedVar = (VariableMember) checkPeeked;
         JavaInstance instance = (JavaInstance) peekedVar.getValue();
         lastIf = (boolean) instance.getInstance();
       }      
       
       if(lastIf) {
         return executeBlock(visitor, executor, ifBlock.getStatements());
       }
     }
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  private BlockExecResult executeWhile(GenVisitor visitor, RuntimeTable executor, WhileLoopBlock whileLoop) {
    whileLoop.getCondition().getExpression().accept(visitor);
    boolean peeked = false;
    StackComponent checkPeeked = visitor.peekStack();
    if (checkPeeked instanceof JavaInstance) {
      peeked = (boolean) ((JavaInstance) checkPeeked).getInstance();
    }
    else {
      VariableMember peekedVar = (VariableMember) checkPeeked;
      JavaInstance instance = (JavaInstance) peekedVar.getValue();
      peeked = (boolean) instance.getInstance();
    }      
    
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
      
      StackComponent boolPeeked = visitor.peekStack();
      if (checkPeeked instanceof JavaInstance) {
        peeked = (boolean) ((JavaInstance) boolPeeked).getInstance();
      }
      else {
        VariableMember peekedVar = (VariableMember) boolPeeked;
        JavaInstance instance = (JavaInstance) peekedVar.getValue();
        peeked = (boolean) instance.getInstance();
      }          
    }        
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  private BlockExecResult executeForLoop(GenVisitor visitor, RuntimeTable executor, ForLoopBlock forLoop) {
    //System.out.println("----FOR LOOP: "+forLoop.getInitialization().getDescription());
    if (forLoop.getInitialization() != null) {
      if (forLoop.getInitialization().getDescription() == StatementDescription.VAR_DEC) {
        Variable variable = (Variable) forLoop.getInitialization();
        VariableMember variableMember = new VariableMember(variable.getName().content(), 
                                                           variable.getExpression(),
                                                           variable.isConstant());
        executor.placeLocalVar(variableMember);
        
        if (variable.getExpression() != null) {
          //System.out.println("---INITIALIZATION"+variable.getExpression().tokens()[0]);
          variable.getExpression().accept(visitor);
          StackComponent peeked = visitor.peekStack();
          if (peeked instanceof Instance) {
            variableMember.setValue((Instance) peeked);
          }
          else {
            VariableMember peekedVar = (VariableMember) peeked;
            variableMember.setValue(peekedVar.getValue());
          }
          //System.out.println("---DONE!!! "+variableMember.getValue().toString()+ " || "+peeked.getValue()+" , "+peeked.getType());
          
          //TODO: remove!!!
          //System.exit(0);
        }
      }
      else {
        forLoop.getInitialization().getExpression().accept(visitor);
      }
    }
    
    
    boolean peeked = true;
    if (forLoop.getCheckStatement() != null) {
      forLoop.getCheckStatement().getExpression().accept(visitor);
      StackComponent checkPeeked = visitor.peekStack();
      if (checkPeeked instanceof JavaInstance) {
        peeked = (boolean) ((JavaInstance) checkPeeked).getInstance();
      }
      else {
        VariableMember peekedVar = (VariableMember) checkPeeked;
        JavaInstance instance = (JavaInstance) peekedVar.getValue();
        peeked = (boolean) instance.getInstance();
      }      
    }        
        
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
            
      //execute change statement
      if (forLoop.getChangeStatement() != null) {
        forLoop.getChangeStatement().getExpression().accept(visitor);
      }

      //execute check statement and change peeked value
      if (forLoop.getCheckStatement() != null) {
        forLoop.getCheckStatement().getExpression().accept(visitor);
            
        StackComponent checkPeeked = visitor.peekStack();
        if (checkPeeked instanceof JavaInstance) {
          peeked = (boolean) ((JavaInstance) checkPeeked).getInstance();
        }
        else {
          VariableMember peekedVar = (VariableMember) checkPeeked;
          JavaInstance instance = (JavaInstance) peekedVar.getValue();
          peeked = (boolean) instance.getInstance();
        }      
      }
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
          StackComponent member = visitor.peekStack();
          if (member.isAnInstance()) {
            return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, (Instance) member);
          }
          else {
            VariableMember peekedVar = (VariableMember) member;          
            return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, peekedVar.getValue());
          }
        }
        else {
          return new BlockExecResult(BlockExecResult.RETURN_ENCOUNTERED, null);
        }
      }
      else if (loopStatement.getDescription() == StatementDescription.VAR_DEC) {
        Variable variable = (Variable) loopStatement;
        VariableMember variableMember = new VariableMember(variable.getName().content(), variable.isConstant());
        if(executor.placeLocalVar(variableMember)) {
          throw new RuntimeException("Duplicate variable '"+variableMember.getName()+"' at line "+
              variable.getExpression().tokens()[0].lineNumber());
        }
        else {
          if (variable.getExpression() != null) {
            variableMember.getExpr().accept(visitor);
            StackComponent member = visitor.peekStack();
            if (member.isAnInstance()) {
              variableMember.setValue((Instance) member);
            }
            else {
              VariableMember peekedVar = (VariableMember) member;          
              variableMember.setValue(peekedVar.getValue());
            }
          }          
        }       
      }
      else if (loopStatement.getDescription() == StatementDescription.BLOCK) {
        StatementBlock block = (StatementBlock) loopStatement;
        executor = executor.clone(true);
        BlockExecResult result = executeStatementBlock(new GenVisitor(executor, currentFile, runtime), executor, block);
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
  
  public boolean argumentsCompatible(Instance ... args) {
    return argAmnt == args.length;
  }
  
  private static class BlockExecResult{
    
    static final int NORMAL_END = 3;
    static final int RETURN_ENCOUNTERED = 0;
    static final int BREAK_ENCOUNTERED = 1;
    static final int CONTINUE_ENCOUNTERED = 2;
    static final int THROWABLE_THROWN = 4;
    
    private final int encounter;
    private Instance got;
    
    public BlockExecResult(int encounter, Instance got) {
      this.encounter = encounter;
      this.got = got;
    }
    
    public Instance getReturnedObject() {
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
