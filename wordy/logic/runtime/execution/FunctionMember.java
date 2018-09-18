package wordy.logic.runtime.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import wordy.logic.compile.structure.CatchBlock;
import wordy.logic.compile.structure.CatchBlock.ExceptionName;
import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Statement.StatementDescription;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.WhileLoopBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;
import wordy.logic.compile.structure.TryBlock;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.TypeChecks;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.components.StackComponent;
import wordy.logic.runtime.errors.InvocationException;
import wordy.logic.runtime.errors.TypeError;
import wordy.logic.runtime.errors.UnfoundClassException;
import wordy.logic.runtime.types.JavaClassDefinition;
import wordy.logic.runtime.types.TypeDefinition;

/**
 * Represents a callable function.
 * @author Jose Guaro
 *
 */
public class FunctionMember extends Callable{

  protected Statement [] statements;
  protected FileInstance currentFile;
  
  private InvocationException lastThrow;
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
        else if (result.gotThrow()) {
          throw new InvocationException(result.getReturnedObject());
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
                                       variable.getName().lineNumber()+", "+currentFile.getName());
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
      else if (statement.getDescription() == StatementDescription.THROW) {
        //check if evaluated type is a child of Java.lang.throwable
        statement.getExpression().accept(visitor);
        StackComponent checkPeeked = visitor.peekStack();
        if (checkPeeked instanceof Instance) {
          Instance actualInstance = (Instance) checkPeeked;
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Throwable.class))) {
            throw new InvocationException(actualInstance);
          }
          throw new TypeError("throw exception doesn't evaluate to a throwable type! ", 
                                   statement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
        }
        else {
          VariableMember peekedVar = (VariableMember) checkPeeked;
          Instance actualInstance = peekedVar.getValue();
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Throwable.class))) {
            throw new InvocationException(actualInstance);
          }
          throw new TypeError("throw exception doesn't evaluate to a throwable type! ", 
                                   statement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
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
    else if (block.blockType() == BlockType.TRY) {
      return executeTry(visitor, executor, (TryBlock) block);
    }
    else if (block.blockType() == BlockType.CATCH) {
      return executeCatch(visitor, executor, (CatchBlock) block);
    }
    else {
      return executeBlock(visitor, executor, block.getStatements());
    }
  }
  
  private BlockExecResult executeCatch(GenVisitor visitor, RuntimeTable executor, CatchBlock catchBlock) {
    if (lastThrow != null) {
      ArrayList<TypeDefinition> exceptionTypes = new ArrayList<>();
      for(ExceptionName exName : catchBlock.getExceptionTypes()) {
        if (exName.getNameArray().length == 1) {
          //simple name. Check for name in file's type def map.
          //If not present, check for imports
          
          if (currentFile.getDefinition().getTypeDefs().containsKey(exName.getName())) {
            exceptionTypes.add(currentFile.getDefinition().getTypeDefs().get(exName.getName()));
          }
          else if (currentFile.getDefinition().getJavaClassMap().containsKey(exName.getName())) {
            String fullClassName = currentFile.getDefinition().getJavaClassMap().get(exName.getName());
            try {
              Class<?> actualClass = Class.forName(fullClassName);
              exceptionTypes.add(JavaClassDefinition.defineClass(actualClass));
            } catch (ClassNotFoundException e) {
              throw new UnfoundClassException(fullClassName, currentFile.getName(), catchBlock.getBlockSig().lineNumber());
            }
          }
        }
        else if (exName.getNameArray().length == 3) {
          //Can be the full binary name of a Wordy class file: filename.classname
          //or can be the binary name of a Java class, but it only has 3 things in it
          //check for wordy class first
          
          TypeDefinition definition = runtime.findTypeDef(exName.getNameArray()[0].content(), 
                                                          exName.getNameArray()[1].content());
          if (definition == null) {
            String fullClassName = exName.getName();
            try {
              Class<?> actualClass = Class.forName(fullClassName);
              exceptionTypes.add(JavaClassDefinition.defineClass(actualClass));
            } catch (ClassNotFoundException e) {
              throw new UnfoundClassException(fullClassName, currentFile.getName(), catchBlock.getBlockSig().lineNumber());
            }
          }
          else {
            exceptionTypes.add(definition);
          }
        }
        else if (exName.getNameArray().length > 3) {
          //this is a full-on java class
          try {
            Class<?> actualClass = Class.forName(exName.getName());
            exceptionTypes.add(JavaClassDefinition.defineClass(actualClass));
          } catch (ClassNotFoundException e) {
            throw new UnfoundClassException(exName.getName(), currentFile.getName(), catchBlock.getBlockSig().lineNumber());
          }
        }
      }
      
      TypeDefinition actualThrowDef = lastThrow.getThrowInstance().getDefinition();
      for(TypeDefinition definition : exceptionTypes) {
        if (actualThrowDef.equals(definition) || actualThrowDef.isChildOf(definition)) {
          executor = executor.clone(true);
          VariableMember exceptionVar = new VariableMember(catchBlock.getVariableName().content(), false);
          exceptionVar.setValue(lastThrow.getThrowInstance());
          executor.placeLocalVar(exceptionVar);
          
          lastThrow = null;
          return executeBlock(new GenVisitor(executor, currentFile, runtime), executor, catchBlock.getStatements());
        }
      }
      
      throw lastThrow;
    }
    else {
      return new BlockExecResult(BlockExecResult.NORMAL_END, null);
    }
   
  }
  
  private BlockExecResult executeTry(GenVisitor visitor, RuntimeTable executor, TryBlock tryBlock) {
    try {
      return executeBlock(visitor, executor, tryBlock.getStatements());
    }
    catch (InvocationException e) {
      lastThrow = e;
      return new BlockExecResult(BlockExecResult.THROWABLE_THROWN, e.getThrowInstance());
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
       
       //System.out.println("---IF VALUE CONDIT: "+lastIf);
       
       if(lastIf) {
         return executeBlock(visitor, executor, ifBlock.getStatements());
       }
     }
    return new BlockExecResult(BlockExecResult.NORMAL_END, null);   
  }
  
  private BlockExecResult executeWhile(GenVisitor visitor, RuntimeTable executor, WhileLoopBlock whileLoop) {
    //System.out.println("----EXECUTING WHILE----");
    whileLoop.getCondition().getExpression().accept(visitor);
    boolean peeked = false;
    StackComponent checkPeeked = visitor.peekStack();
    if (checkPeeked instanceof JavaInstance) {
      peeked = TypeChecks.getBooleanEquivalent(((JavaInstance) checkPeeked).getInstance());
      //System.out.println("----RESULTS OF COND: "+peeked);
    }
    else {
      VariableMember peekedVar = (VariableMember) checkPeeked;
      JavaInstance instance = (JavaInstance) peekedVar.getValue();
      peeked = TypeChecks.getBooleanEquivalent(instance.getInstance());
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
      RuntimeTable loopExecutor = executor.clone(true);
      GenVisitor loopVisitor = new GenVisitor(loopExecutor, currentFile, runtime);
      BlockExecResult result = executeBlock(loopVisitor, loopExecutor, forLoop.getStatements());
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
        VariableMember variableMember = new VariableMember(variable.getName().content(), 
                                                           variable.getExpression(), 
                                                           variable.isConstant());
        if(executor.placeLocalVar(variableMember)) {
          throw new RuntimeException("Duplicate variable '"+variableMember.getName()+"' at line "+
              variableMember.getExpr().tokens()[0].lineNumber());
        }
        else {
          if (variableMember.getExpr() != null) {
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
      else if (loopStatement.getDescription() == StatementDescription.THROW) {
        //check if evaluated type is a child of Java.lang.throwable
        loopStatement.getExpression().accept(visitor);
        StackComponent checkPeeked = visitor.peekStack();
        if (checkPeeked instanceof Instance) {
          Instance actualInstance = (Instance) checkPeeked;
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Throwable.class))) {
            throw new InvocationException(actualInstance);
          }
          throw new TypeError("throw exception doesn't evaluate to a throwable type! ", 
                                   loopStatement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
        }
        else {
          VariableMember peekedVar = (VariableMember) checkPeeked;
          Instance actualInstance = peekedVar.getValue();
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Throwable.class))) {
            throw new InvocationException(actualInstance);
          }
          throw new TypeError("throw exception doesn't evaluate to a throwable type! ", 
                                   loopStatement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
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
  
  public Statement[] getStatements() {
    return statements;
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
    
    public boolean gotThrow() {
      return encounter == THROWABLE_THROWN;
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
