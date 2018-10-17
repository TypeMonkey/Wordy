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
import wordy.logic.runtime.errors.FatalInternalException;
import wordy.logic.runtime.errors.InvocationException;
import wordy.logic.runtime.errors.UnfoundClassException;
import wordy.logic.runtime.execution.GenVisitor.CarrierInvocationException;
import wordy.logic.runtime.types.JavaClassDefinition;
import wordy.logic.runtime.types.TypeDefinition;
import wordy.standard.Exception;

/**
 * Represents a callable function.
 * @author Jose Guaro
 *
 */
public class FunctionMember extends Callable{

  protected final Statement [] statements;
  protected final FileInstance currentFile;
  
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
  public Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args) throws InvocationException{    
    //System.out.println("*****---CALLED: "+getName()+" | ");
    table.addFuncMap(currentFile.getDefinition().getFunctions());
    int argCnt = argAmnt;
    for(Statement statement: statements) {
      //System.out.println("------NEXT STATEMENT------ || "+currentFile.getName());
      visitor.resetStack();
      
      /*
       * Check for already present thrown exception
       */
      if (lastThrow != null) {
        if (statement instanceof CatchBlock) {
          RuntimeTable catchTable = table.clone(true);
          BlockExecResult catchResult = executeCatch(new GenVisitor(catchTable, currentFile, runtime), 
                                                     catchTable, 
                                                     (CatchBlock) statement);
          if (catchResult.gotBreak()) {
            break;
          }
          else if (catchResult.gotContinue()) {
            continue;
          }
          else if (catchResult.gotReturn()) {
            return catchResult.getReturnedObject();
          }
        }
        else {
          lastThrow.registerTrace(currentFile.getName(), statement.getExpression().locationToken().lineNumber());
          throw lastThrow;
        }
      }
      else {
        /*
         * If not thrown exception, then execute normally 
         */
        if (statement.getDescription() == StatementDescription.BLOCK) {
          RuntimeTable blockExec = table.clone(true);
          
          try {
            BlockExecResult result = executeStatementBlock(new GenVisitor(blockExec, currentFile, runtime), 
                                                           blockExec, (StatementBlock) statement);
            if (result.gotBreak()) {
              break;
            }
            else if (result.gotContinue()) {
              continue;
            }
            else if (result.gotReturn()) {
              return result.getReturnedObject();
            }
          } catch (InvocationException e) {
            //System.out.println("---CAUGHT: "+e);
            lastThrow = e;
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
                try {
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
                } catch (CarrierInvocationException e) {
                  InvocationException invocationException = e.getException();
                  invocationException.registerTrace(currentFile.getName(), 
                      variable.getExpression().locationToken().lineNumber());

                  throw invocationException;
                }
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
            try {
              statement.getExpression().accept(visitor);
              StackComponent checkPeeked = visitor.peekStack();
              if (checkPeeked instanceof Instance) {
                return (Instance) checkPeeked;
              }
              else {
                VariableMember peekedVar = (VariableMember) checkPeeked;
                return peekedVar.getValue();
              }      
            } catch (CarrierInvocationException e) {
              InvocationException invocationException = e.getException();
              invocationException.registerTrace(currentFile.getName(), 
                  statement.getExpression().locationToken().lineNumber());

              throw invocationException;
            }
          }
        }
        else if (statement.getDescription() == StatementDescription.THROW) {
          //check if evaluated type is a child of Java.lang.throwable
          statement.getExpression().accept(visitor);
          StackComponent checkPeeked = visitor.peekStack();
          if (checkPeeked instanceof Instance) {
            Instance actualInstance = (Instance) checkPeeked;
            if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Exception.class))) {
              //System.out.println("-----exception: "+statement.getExpression().locationToken().lineNumber());
              InvocationException exception = new InvocationException(actualInstance, runtime, table, currentFile);
              exception.registerTrace(currentFile.getName(), statement.getExpression().locationToken().lineNumber());
              
              lastThrow = exception;
            }
            else {
              throw new FatalInternalException("throw exception doesn't evaluate to a throwable type! ", 
                  statement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
            }            
          }
          else {
            VariableMember peekedVar = (VariableMember) checkPeeked;
            Instance actualInstance = peekedVar.getValue();
            if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Exception.class))) {
              InvocationException exception = new InvocationException(actualInstance, runtime, table, currentFile);
              exception.registerTrace(currentFile.getName(), statement.getExpression().locationToken().lineNumber());
              
              lastThrow = exception;
            }
            else {
              throw new FatalInternalException("throw exception doesn't evaluate to a throwable type! ", 
                  statement.getExpression().tokens()[0].lineNumber(), currentFile.getName());
            }
          }      
        }
        else {
          //System.out.println("---EXEC NORM EXPR: "+statement.getExpression().getClass().getName());
          statement.getExpression().accept(visitor);
        } 
      }
    }
    
    //System.out.println("*****^^ FINISH CALL "+name+" | ");
    /*
     * An exception has been successfully caught in the current function when 'lasthrow' has been nulled out
     * But what about the case in which an exception has been thrown, but there are no more statements to execute.
     * 
     * EX: function example(){
     *        println("hello world");
     *        throw Exception("bye world");  //this would not be passed on to the caller
     *     }
     */
    if (lastThrow != null) {
      throw lastThrow;
    }
    return null;
  }
  
  private BlockExecResult executeStatementBlock(GenVisitor visitor, RuntimeTable executor, StatementBlock block) throws InvocationException {
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
      return executeBlock(visitor, executor, block.getStatements(), false);
    }
  }
  
  private BlockExecResult executeCatch(GenVisitor visitor, RuntimeTable executor, CatchBlock catchBlock) throws InvocationException {
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
      
      //System.out.println("---IN CATCH! "+catchBlock.getExceptionTypes().length);
      
      TypeDefinition actualThrowDef = lastThrow.getThrowInstance().getDefinition();
      for(TypeDefinition definition : exceptionTypes) {
        if (actualThrowDef.equals(definition) || actualThrowDef.isChildOf(definition)) {
          executor = executor.clone(true);
          VariableMember exceptionVar = new VariableMember(catchBlock.getVariableName().content(), false);
          exceptionVar.setValue(lastThrow.getThrowInstance());
          executor.placeLocalVar(exceptionVar);

          //System.out.println("---EXECUTING CATCH CODE "+actualThrowDef.getName());
          lastThrow = null;
          return executeBlock(new GenVisitor(executor, currentFile, runtime), executor, catchBlock.getStatements(), true);
        }
      }
      
      //if any of the catch block's exception types are not parent's or is the type of the 
      //exception, then throw the exception. (bubble it up)
      throw lastThrow;
    }
    else {
      return new BlockExecResult(BlockExecResult.NORMAL_END, null);
    }
   
  }
  
  private BlockExecResult executeTry(GenVisitor visitor, RuntimeTable executor, TryBlock tryBlock) throws InvocationException {
     return executeBlock(visitor, executor, tryBlock.getStatements(), false);
  }
  
  private BlockExecResult executeIf(GenVisitor visitor, RuntimeTable executor, IfBlock ifBlock) throws InvocationException {   
    try {
      if(ifBlock.isElseIf()) {
        if(lastIf == false){
          if(ifBlock.getCondition() == null) {
            /*
             * Is just a pure else block. A.k.a: else{ }
             */
            return executeBlock(visitor, executor, ifBlock.getStatements(), false);
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
              return executeBlock(visitor, executor, ifBlock.getStatements(), false);
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
          return executeBlock(visitor, executor, ifBlock.getStatements(), false);
        }
      }
      return new BlockExecResult(BlockExecResult.NORMAL_END, null);
    } catch (CarrierInvocationException e) {
      InvocationException invocationException = e.getException();
      invocationException.registerTrace(currentFile.getName(), 
          ifBlock.getCondition().getExpression().locationToken().lineNumber());

      throw invocationException;
    }
  }
  
  private BlockExecResult executeWhile(GenVisitor visitor, RuntimeTable executor, WhileLoopBlock whileLoop) throws InvocationException {
    //System.out.println("----EXECUTING WHILE----");
    try {
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
        BlockExecResult result = executeBlock(visitor, executor, whileLoop.getStatements(), false);
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
    } catch (CarrierInvocationException e) {
      InvocationException invocationException = e.getException();
      invocationException.registerTrace(currentFile.getName(), 
                                        whileLoop.getCondition().getExpression().locationToken().lineNumber());

      throw invocationException;
    }
  }
  
  private BlockExecResult executeForLoop(GenVisitor visitor, RuntimeTable executor, ForLoopBlock forLoop) throws InvocationException {
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
          
          try {
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
          } catch (CarrierInvocationException e) {
            InvocationException invocationException = e.getException();
            invocationException.registerTrace(currentFile.getName(), 
                                              forLoop.getCheckStatement().getExpression().locationToken().lineNumber());

            throw invocationException;
          }
        }
      }
      else {
        forLoop.getInitialization().getExpression().accept(visitor);
      }
    }
    
    
    boolean peeked = true;
    if (forLoop.getCheckStatement() != null) {
      try {
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
      } catch (CarrierInvocationException e) {
        InvocationException invocationException = e.getException();
        invocationException.registerTrace(currentFile.getName(), 
                                          forLoop.getCheckStatement().getExpression().locationToken().lineNumber());

        throw invocationException;
      }
    }        
        
    
    while(peeked) {
      RuntimeTable loopExecutor = executor.clone(true);
      GenVisitor loopVisitor = new GenVisitor(loopExecutor, currentFile, runtime);
      
      try {
        BlockExecResult result = executeBlock(loopVisitor, loopExecutor, forLoop.getStatements(), false);
        if (result.gotBreak()) {
          break;
        }
        else if (result.gotContinue()) {
          continue;
        }
        else if (result.gotReturn()) {
          return result;
        }
      } catch (InvocationException e) {
        lastThrow = e;
      }
            
      //execute change statement
      if (forLoop.getChangeStatement() != null) {
        try {
          forLoop.getChangeStatement().getExpression().accept(visitor);
        } catch (CarrierInvocationException e) {
          InvocationException invocationException = e.getException();
          invocationException.registerTrace(currentFile.getName(), 
                                            forLoop.getCheckStatement().getExpression().locationToken().lineNumber());

          throw invocationException;
        }
      }

      //execute check statement and change peeked value
      if (forLoop.getCheckStatement() != null) {
        try {
          forLoop.getCheckStatement().getExpression().accept(visitor);
        } catch (CarrierInvocationException e) {
          InvocationException invocationException = e.getException();
          invocationException.registerTrace(currentFile.getName(), 
                                            forLoop.getCheckStatement().getExpression().locationToken().lineNumber());

          throw invocationException;
        }
            
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
  
  private BlockExecResult executeBlock(GenVisitor visitor, RuntimeTable executor, List<Statement> statements, boolean forCatch) throws InvocationException {
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
          
          try {
            loopStatement.getExpression().accept(visitor);
          } catch (CarrierInvocationException e) {
            InvocationException invocationException = e.getException();
            invocationException.registerTrace(currentFile.getName(), loopStatement.getExpression().locationToken().lineNumber());

            throw invocationException;
          }
          
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
            try {
              variableMember.getExpr().accept(visitor);
            } catch (CarrierInvocationException e) {
              InvocationException invocationException = e.getException();
              invocationException.registerTrace(currentFile.getName(), loopStatement.getExpression().locationToken().lineNumber());
              
              throw invocationException;
            }
            
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
        try {
          loopStatement.getExpression().accept(visitor);
        } catch (CarrierInvocationException e) {
          InvocationException invocationException = e.getException();
          if (forCatch && lastThrow != null) {
            invocationException.registerCause(lastThrow);
            lastThrow = null;
          }
          invocationException.registerTrace(currentFile.getName(), loopStatement.getExpression().locationToken().lineNumber());
          
          throw invocationException;
        }
        
        StackComponent checkPeeked = visitor.peekStack();
        if (checkPeeked instanceof Instance) {
          Instance actualInstance = (Instance) checkPeeked;
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Exception.class))) {
            InvocationException exception = new InvocationException(actualInstance, runtime, executor, currentFile);
            if (forCatch && lastThrow != null) {
              exception.registerCause(lastThrow);
              lastThrow = null;
            }
            exception.registerTrace(currentFile.getName(), 
                                    loopStatement.getExpression().locationToken().lineNumber());
            throw exception;
          }
          throw new FatalInternalException(currentFile.getName(), 
                                   loopStatement.getExpression().locationToken().lineNumber(),  
                                   "throw exception doesn't evaluate to a throwable type!");
        }
        else {
          VariableMember peekedVar = (VariableMember) checkPeeked;
          Instance actualInstance = peekedVar.getValue();
          if (actualInstance.getDefinition().isChildOf(JavaClassDefinition.defineClass(Exception.class))) {
            InvocationException exception = new InvocationException(actualInstance, runtime, executor, currentFile);
            if (forCatch && lastThrow != null) {
              exception.registerCause(lastThrow);
              lastThrow = null;
            }
            exception.registerTrace(currentFile.getName(), 
                                    loopStatement.getExpression().locationToken().lineNumber());
            throw exception;
          }
          throw new FatalInternalException(currentFile.getName(), 
                                    loopStatement.getExpression().locationToken().lineNumber(),  
                                    "throw exception doesn't evaluate to a throwable type!");
        }      
      }
      else {
        try {
          loopStatement.getExpression().accept(visitor);
        } catch (CarrierInvocationException e) {
          InvocationException invocationException = e.getException();
          if (forCatch && lastThrow != null) {
            invocationException.registerCause(lastThrow);
            lastThrow = null;
          }
          invocationException.registerTrace(currentFile.getName(), loopStatement.getExpression().locationToken().lineNumber());
          
          throw invocationException;
        }
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
