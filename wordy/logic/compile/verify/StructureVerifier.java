package wordy.logic.compile.verify;

import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.structure.CatchBlock;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.ImportedFile;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;
import wordy.logic.compile.structure.TryBlock;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.WhileLoopBlock;
import wordy.logic.compile.structure.Statement.StatementDescription;

/**
 * Checks the provided file structure of any problems
 * in structure organization
 * 
 * Examples of such problems are invalid else blocks which
 * have no proceeding if/if-else blocks , undefined members 
 * (functions, variables, and classes) in expressions, etc.
 * 
 * @author Jose Guaro
 *
 */
public class StructureVerifier {
  
  private FileStructure structure;
  private List<FunctionKey> systemFunctions;
  private boolean ifWasEncountered;
  
  private Map<String, FileStructure> otherFiles;
  
  public StructureVerifier(FileStructure structure, 
                           List<FunctionKey> systemFunctions, 
                           Map<String, FileStructure> otherFiles) {
    this.structure = structure;
    this.systemFunctions = systemFunctions;
    this.otherFiles = otherFiles;
  }
  
  public void verify() {
    SymbolTable table = new SymbolTable(otherFiles);
    
    /*
     * Place system classes (Java standard classes) into the system class map
     * along with imports (use alias in place if present) 
     */
    for(String standard: WordyCompiler.JAVA_CLASSES) {
      String [] split = standard.split("\\.");
      String simpleName = split[split.length-1];
      table.placeImportedClass(simpleName);
    }
    
    for(ImportedFile file : structure.getImports()) {
      if(file.getAlias() != null) {
        table.placeImportedClass(file.getAlias().content());
      }
      else {
        table.placeImportedClass(file.getTypeNameImported());
      }
    }
    
    /*
     * Place constructors in function map
     */
    for(ClassStruct struct: structure.getClasses()) {
      for(Function potConstructor: struct.getFunctions()) {
        if (potConstructor.isConstructor()) {
          table.placeFunction(potConstructor);
        }
      }
    }
    
    /*
     * Place the fuction keys of system functions 
     */
    for(FunctionKey key: systemFunctions) {
      table.placeSystemFunction(key);
    }
    
    /*
     * Place all file members in the symbol table, except file variables
     * as we need to check if their values have defined members
     */
    for(Function function: structure.getFunctions()) {
      table.placeFunction(function);
    }
        
    for(ClassStruct struct: structure.getClasses()) {
      table.placeClass(struct);
    }
    
    /*
     * start the actual checking
     * start by checking file variables
     */
     
    for(Variable variable : structure.getVariables()) {
      /*
       * Place variable prior so that the variable's experession value
       * can refer to itself.
       * 
       * Ex: let a = a + 1; <--so it won't throw an error here
       */
      table.placeVariable(variable);
      VerifierVisitor visitor = new VerifierVisitor(table, null);
      ASTNode value = variable.getExpression();
      if (value != null) {
        value.accept(visitor);
      }
    }
    
    /*
     * Then start with the file functions
     */
    for(Function function: structure.getFunctions()) {
      SymbolTable funcTable = table.clone();
      verifyFunction(function, funcTable, null);
      ifWasEncountered = false;
    }
    
    /*
     * Then finally end with the class functions
     */
    for(ClassStruct struct : structure.getClasses()) {
      //first with the ckass variables
      SymbolTable funcTable = table.clone();
      for(Variable classVar: struct.getVariables()) {
        table.placeVariable(classVar);
        if (classVar.getExpression() != null) {
          VerifierVisitor visitor = new VerifierVisitor(table, null);
          classVar.getExpression().accept(visitor);          
        }
      }
      
      /*
       * Then start with the file functions
       */
      for(Function function: struct.getFunctions()) {
        SymbolTable nestedFunc = funcTable.clone();
        verifyFunction(function, nestedFunc, struct.getName());
      }
    }
  }
  
  private void verifyFunction(Function function, SymbolTable funcTable, Token className) {
    VerifierVisitor visitor = new VerifierVisitor(funcTable, className);

    TryBlock recentTryBlock = null;
    
    Statement [] statements = function.getStatements();
    for(Statement statement : statements) {
      if (statement.getDescription() == StatementDescription.VAR_DEC) {
        Variable variable = (Variable) statement;
        funcTable.placeVariable(variable);
        if (variable.getExpression() != null) {
          variable.getExpression().accept(visitor);
        }
      }
      else if (statement.getDescription() == StatementDescription.BLOCK) {
        SymbolTable blockTable = funcTable.clone();
        StatementBlock block = (StatementBlock) statement;
        boolean insideALoop = false;
        System.out.println("---NEXT-F: "+block.blockType()+" | "+block.getBlockSig().lineNumber());
        
        if(recentTryBlock != null && block.blockType() != BlockType.CATCH) {
          throw new ParseError("Invalid try block placement", recentTryBlock.getBlockSig().lineNumber());
        }
        
        if(block.blockType() == BlockType.IF) {
          IfBlock ifBlock = (IfBlock) block;
          if(ifBlock.isElseIf()) {
            if(ifWasEncountered == false) {
              throw new ParseError("Invalid else block placement", ifBlock.getBlockSig().lineNumber());
            }
            else {
              if(ifBlock.getCondition() == null){
                ifWasEncountered  = false;
              }
              else {
                ifBlock.getCondition().getExpression().accept(visitor);
                ifWasEncountered = true;
              }
            }
          }
          else {
            ifBlock.getCondition().getExpression().accept(visitor);
            ifWasEncountered = true;
          }
        }
        else if (block.blockType() == BlockType.FOR) {
          ForLoopBlock forLoopBlock = (ForLoopBlock) block;
          if (forLoopBlock.getInitialization() != null) {
            Statement init = forLoopBlock.getInitialization();
            if(init.getDescription() == StatementDescription.VAR_DEC) {
              Variable variable = (Variable) statement;
              blockTable.placeVariable(variable);
              if (variable.getExpression() != null) {
                variable.getExpression().accept(visitor);
              }
            }
            else {
              if (init.getExpression() != null) {
                init.getExpression().accept(visitor);
              }
            }
          }
          if (forLoopBlock.getCheckStatement() != null) {
            Statement comp = forLoopBlock.getCheckStatement();
            comp.getExpression().accept(visitor);
          }
          if(forLoopBlock.getChangeStatement() != null) {
            Statement change = forLoopBlock.getChangeStatement();
            change.getExpression().accept(visitor);
          }
          insideALoop = true;
        }
        else if (block.blockType() == BlockType.WHILE) {
          WhileLoopBlock whileLoop = (WhileLoopBlock) block;
          whileLoop.getExpression().accept(visitor);
          insideALoop = true;
        }
        else if (block.blockType() == BlockType.TRY) {
          System.out.println("---TRY-F!!!!! "+block.getBlockSig().lineNumber());
          recentTryBlock = (TryBlock) block;
          insideALoop = false;
        }
        else if (block.blockType() == BlockType.CATCH) {
          System.out.println("---CATCH-F!!!!! "+block.getBlockSig().lineNumber());
          if(recentTryBlock == null) {
            throw new ParseError("Invalid catch block placement", block.getBlockSig().lineNumber());
          }
          else {
            recentTryBlock = null;
            insideALoop = false;
          }
        }
        
        System.out.println("---GOING: "+insideALoop);
        verifyBlock(block, blockTable, className, insideALoop);
        insideALoop = false;
      }
      else {
        if(statement.getDescription() == StatementDescription.BREAK || 
            statement.getDescription() == StatementDescription.CONTINUE) {         
          throw new ParseError("'break' and 'continue' statements must be inside loops"
              , statement.getExpression().tokens()[0].lineNumber());       
        }
        else if (statement.getDescription() == StatementDescription.RETURN) {
          if (statement.getExpression() != null) {
            statement.getExpression().accept(visitor);
          }
        }
        else {
          System.out.println("---IS A BREAK? "+statement.getDescription());       
          statement.getExpression().accept(visitor);
        }
      }
    }
    
    //dangling try block
    if(recentTryBlock != null) {
      throw new ParseError("Invalid try block placement", recentTryBlock.getBlockSig().lineNumber());
    }
  }
  
  private void verifyBlock(StatementBlock block, SymbolTable table, Token className, boolean insideALoop) {
    VerifierVisitor visitor = new VerifierVisitor(table, className);
    
    TryBlock recentTryBlock = null;
    
    for(Statement statement : block.getStatements()) {
      System.out.println("----BLOCK EXEC---- "+insideALoop+"||"+statement);
      if (statement.getDescription() == StatementDescription.VAR_DEC) {
        table.placeVariable((Variable) statement);
        Variable variable = (Variable) statement;
        if (variable.getExpression() != null) {
          variable.getExpression().accept(visitor);
        }
      }
      else if (statement.getDescription() == StatementDescription.BLOCK) {
        SymbolTable blockTable = table.clone();
        StatementBlock nestedBlock = (StatementBlock) statement;
        boolean nestedInsideLoop = insideALoop;
        
        System.out.println("---NEXT: "+nestedBlock.blockType()+" | "+nestedBlock.getBlockSig().lineNumber());
        if(recentTryBlock != null && nestedBlock.blockType() != BlockType.CATCH) {
          throw new ParseError("Invalid try block placement", recentTryBlock.getBlockSig().lineNumber());
        }       
        
        if(nestedBlock.blockType() == BlockType.IF) {
          IfBlock ifBlock = (IfBlock) nestedBlock;
          if(ifBlock.isElseIf()) {
            if(ifWasEncountered == false) {
              String message = "Invalid else block placement";
              throw new ParseError(message, ifBlock.getBlockSig().lineNumber());
            }
            else {
              if(ifBlock.getCondition() == null){
                ifWasEncountered  = false;
              }
              else {
                ifBlock.getCondition().getExpression().accept(visitor);
                ifWasEncountered = true;
              }
            }
          }
          else {
            ifBlock.getCondition().getExpression().accept(visitor);
            ifWasEncountered = true;
          }
        }
        else if (nestedBlock.blockType() == BlockType.FOR) {
          ForLoopBlock forLoopBlock = (ForLoopBlock) nestedBlock;
          if (forLoopBlock.getInitialization() != null) {
            Statement init = forLoopBlock.getInitialization();
            if(init.getDescription() == StatementDescription.VAR_DEC) {
              Variable variable = (Variable) statement;
              blockTable.placeVariable(variable);
              if (variable.getExpression() != null) {
                variable.getExpression().accept(visitor);
              }
            }
            else {
              if (init.getExpression() != null) {
                init.getExpression().accept(visitor);
              }
            }
          }
          if (forLoopBlock.getCheckStatement() != null) {
            Statement comp = forLoopBlock.getCheckStatement();
            comp.getExpression().accept(visitor);
          }
          if(forLoopBlock.getChangeStatement() != null) {
            Statement change = forLoopBlock.getChangeStatement();
            change.getExpression().accept(visitor);
          }
          nestedInsideLoop = true;
        }
        else if (nestedBlock.blockType() == BlockType.WHILE) {
          WhileLoopBlock whileLoop = (WhileLoopBlock) nestedBlock;
          whileLoop.getExpression().accept(visitor);
          nestedInsideLoop = true;
        }
        else if (nestedBlock.blockType() == BlockType.TRY) {
          System.out.println("-----TRY!!!!! "+block.getBlockSig().lineNumber());
          recentTryBlock = (TryBlock) nestedBlock;
          nestedInsideLoop = false;
        }
        else if (nestedBlock.blockType() == BlockType.CATCH) {
          System.out.println("---CATCH!!!!! "+block.getBlockSig().lineNumber());
          if(recentTryBlock == null) {
            throw new ParseError("Invalid catch block placement", nestedBlock.getBlockSig().lineNumber());
          }
          else {
            recentTryBlock = null;
            nestedInsideLoop = false;
          }
        }
        verifyBlock(nestedBlock, table, className, nestedInsideLoop);     
      }
      else {
        if(statement.getDescription() == StatementDescription.BREAK || 
            statement.getDescription() == StatementDescription.CONTINUE) {
          if (insideALoop == false) {
            throw new ParseError("'break' and 'continue' statements must be inside loops"
                , statement.getExpression().tokens()[0].lineNumber());
          }
        }
        else {
          statement.getExpression().accept(visitor);
        }
      }
      
      
      System.out.println("----BLOCK EXEC END---- ");
    }
    
    //dangling try block
    if(recentTryBlock != null) {
      throw new ParseError("Invalid try block placement", recentTryBlock.getBlockSig().lineNumber());
    }
  }
}
