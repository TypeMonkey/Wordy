package wordy.logic.compile.verify;

import java.util.List;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.ForLoopBlock;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.IfBlock;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.StatementBlock;
import wordy.logic.compile.structure.StatementBlock.BlockType;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.WhileLoopBlock;

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
  
  public StructureVerifier(FileStructure structure, List<FunctionKey> systemFunctions) {
    this.structure = structure;
    this.systemFunctions = systemFunctions;
  }
  
  public void verify() {
    SymbolTable table = new SymbolTable();
    
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
        value.visit(visitor);
      }
    }
    
    /*
     * Then start with the file functions
     */
    for(Function function: structure.getFunctions()) {
      SymbolTable funcTable = table.clone();
      verifyFunction(function, funcTable, null);
    }
    
    ifWasEncountered = false;
    
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
          classVar.getExpression().visit(visitor);          
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

    Statement [] statements = function.getStatements();
    for(Statement statement : statements) {
      if (statement.isAVarDec()) {
        Variable variable = (Variable) statement;
        funcTable.placeVariable(variable);
        if (variable.getExpression() != null) {
          variable.getExpression().visit(visitor);
        }
      }
      else if (statement.isABlock()) {
        SymbolTable blockTable = funcTable.clone();
        StatementBlock block = (StatementBlock) statement;
        boolean insideALoop = false;
        if(block.blockType() == BlockType.IF) {
          IfBlock ifBlock = (IfBlock) block;
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
                ifBlock.getCondition().getExpression().visit(visitor);
                ifWasEncountered = true;
              }
            }
          }
          else {
            ifBlock.getCondition().getExpression().visit(visitor);
            ifWasEncountered = true;
          }
        }
        else if (block.blockType() == BlockType.FOR) {
          ForLoopBlock forLoopBlock = (ForLoopBlock) block;
          if (forLoopBlock.getInitialization() != null) {
            Statement init = forLoopBlock.getInitialization();
            if(init.isAVarDec()) {
              Variable variable = (Variable) statement;
              blockTable.placeVariable(variable);
              if (variable.getExpression() != null) {
                variable.getExpression().visit(visitor);
              }
            }
            else {
              if (init.getExpression() != null) {
                init.getExpression().visit(visitor);
              }
            }
          }
          if (forLoopBlock.getCheckStatement() != null) {
            Statement comp = forLoopBlock.getCheckStatement();
            comp.getExpression().visit(visitor);
          }
          if(forLoopBlock.getChangeStatement() != null) {
            Statement change = forLoopBlock.getChangeStatement();
            change.getExpression().visit(visitor);
          }
          insideALoop = true;
        }
        else if (block.blockType() == BlockType.WHILE) {
          WhileLoopBlock whileLoop = (WhileLoopBlock) block;
          whileLoop.getExpression().visit(visitor);
          insideALoop = true;
        }
        System.out.println("---GOING: "+insideALoop);
        verifyBlock(block, blockTable, className, insideALoop);
        insideALoop = false;
      }
      else {
        if(statement.isABreak() || statement.isAContinue()) {         
          throw new ParseError("'break' and 'continue' statements must be inside loops"
              , statement.getExpression().tokens()[0].lineNumber());       
        }
        else {
          System.out.println("---IS A BREAK? "+statement.isABreak());
          statement.getExpression().visit(visitor);
        }
      }
    }
  }
  
  private void verifyBlock(StatementBlock block, SymbolTable table, Token className, boolean insideALoop) {
    VerifierVisitor visitor = new VerifierVisitor(table, className);
    for(Statement statement : block.getStatements()) {
      System.out.println("----BLOCK EXEC---- "+insideALoop);
      if (statement.isAVarDec()) {
        table.placeVariable((Variable) statement);
        Variable variable = (Variable) statement;
        if (variable.getExpression() != null) {
          variable.getExpression().visit(visitor);
        }
      }
      else if (statement.isABlock()) {
        SymbolTable blockTable = table.clone();
        StatementBlock nestedBlock = (StatementBlock) statement;
        boolean nestedInsideLoop = insideALoop;
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
                ifBlock.getCondition().getExpression().visit(visitor);
                ifWasEncountered = true;
              }
            }
          }
          else {
            ifBlock.getCondition().getExpression().visit(visitor);
            ifWasEncountered = true;
          }
        }
        else if (nestedBlock.blockType() == BlockType.FOR) {
          ForLoopBlock forLoopBlock = (ForLoopBlock) nestedBlock;
          if (forLoopBlock.getInitialization() != null) {
            Statement init = forLoopBlock.getInitialization();
            if(init.isAVarDec()) {
              Variable variable = (Variable) statement;
              blockTable.placeVariable(variable);
              if (variable.getExpression() != null) {
                variable.getExpression().visit(visitor);
              }
            }
            else {
              if (init.getExpression() != null) {
                init.getExpression().visit(visitor);
              }
            }
          }
          if (forLoopBlock.getCheckStatement() != null) {
            Statement comp = forLoopBlock.getCheckStatement();
            comp.getExpression().visit(visitor);
          }
          if(forLoopBlock.getChangeStatement() != null) {
            Statement change = forLoopBlock.getChangeStatement();
            change.getExpression().visit(visitor);
          }
          nestedInsideLoop = true;
        }
        else if (nestedBlock.blockType() == BlockType.WHILE) {
          WhileLoopBlock whileLoop = (WhileLoopBlock) nestedBlock;
          whileLoop.getExpression().visit(visitor);
          nestedInsideLoop = true;
        }
        verifyBlock(nestedBlock, table, className, nestedInsideLoop);
        
      }
      else {
        if(statement.isABreak() || statement.isAContinue()) {
          if (insideALoop == false) {
            throw new ParseError("'break' and 'continue' statements must be inside loops"
                , statement.getExpression().tokens()[0].lineNumber());
          }
        }
        else {
          statement.getExpression().visit(visitor);
        }
      }
      System.out.println("----BLOCK EXEC END----");
    }
  }
}
