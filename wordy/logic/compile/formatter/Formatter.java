package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import javax.management.RuntimeErrorException;

import wordy.logic.Main;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Variable;
import wordy.logic.compile.structure.Function.FunctionBuilder;

/**
 * Forms the structures and members of a source file
 * @author Jose Guaro
 *
 */
public class Formatter{  
  private List<Token> tokens;
  private String fileName;
  
  public Formatter(List<Token> tokens, String fileName) {
    this.tokens = tokens;
    this.fileName = fileName;
  }
  
  public FileStructure formatSource() {
    FileStructure fileStructure = new FileStructure(fileName);
    parseDeclarations(fileStructure);
    return fileStructure;
  }
  
  private void parseDeclarations(FileStructure structure) {
    
    ListIterator<Token> iterator = tokens.listIterator();
    
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.LET, Type.FUNCTION, Type.CLASS));
    while (iterator.hasNext()) {
      Token current = iterator.next();
      if (expected.contains(current.type())) {
        if (current.type() == Type.LET) {
          ArrayList<Token> varDecTokens = new ArrayList<>();
          varDecTokens.add(current);
          varDecTokens.addAll(gatherStatement(iterator, current.lineNumber()));
          varDecTokens.remove(varDecTokens.size() - 1);
                    
          if (varDecTokens.isEmpty() == false) {
            VariableFormatter varFormatter = new VariableFormatter(varDecTokens);
            Variable variable = varFormatter.formVariable();
            if (structure.addVariable(variable)) {
              throw new ParseError("Duplicate variable definition of '"+variable.getName().content()+"' ",
                                    current.lineNumber());
            }
            
            expected.clear();
            expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.CLASS, Type.NO_EXPECT));
          }
        }
        else if (current.type() == Type.FUNCTION) {
          Function function = parseFunction(iterator, false);
          
          /*
           * Check is there's already a function with the same name.
           * If not, then check if a function has the same name as a class
           */
          if(structure.addFunction(function)) {
            throw new RuntimeException("Duplicate function definition at line "+current.lineNumber());
          }
          else {
            ClassStruct struct = structure.getClass(function.getName().content());
            if (struct != null) {
              String message = "The class '"+function.getName().content()+"' and the function '"+struct.getName().content()+"' "+
                               System.lineSeparator()+" have the same name";
              throw new ParseError(message, current.lineNumber());
            }
          }
          
          expected.clear();
          expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.CLASS, Type.NO_EXPECT));
        }
        else if (current.type() == Type.CLASS) {
          ClassStruct struct = parseClassDeclaration(iterator);
          if (structure.addClass(struct)) {
            throw new RuntimeException("Duplicate class declaration of "+struct.getName().content()+
                                       " at line "+struct.getName().lineNumber());
          }
          
          expected.clear();
          expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.CLASS, Type.NO_EXPECT));
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content()+"'", current.lineNumber());
      }
    }
  }
  
  private Function parseFunction(ListIterator<Token> iterator, boolean isConstructor) {
    System.out.println("------PARSING FUNCTION-------");
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.IDENT));
    
    FunctionBuilder functionBuilder = new FunctionBuilder();
    
    boolean parsingParams = false;
    
    List<Token> bodyTokens = new ArrayList<>();
    boolean currentParamIsConstant = false;
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("--FUNC CUR: "+current);
      if (expected.contains(current.type())) {
        if (current.type() == Type.IDENT) {
          if (parsingParams) {
            /*
             * With this, we know that the first statements of our function - in internal representation -
             * are the function parameters of this function.
             * 
             */
            functionBuilder.addStatement(new Variable(current, currentParamIsConstant), true);
            currentParamIsConstant = false;
            expected.clear();
            expected.addAll(Arrays.asList(Type.COMMA, Type.RIGHT_PAREN));
          }
          else {
            functionBuilder.setName(current);
            expected.clear();
            expected.add(Type.LEFT_PAREN);
          }
        }
        else if (current.type() == Type.LEFT_PAREN) {
          expected.clear();
          expected.addAll(Arrays.asList(Type.IDENT, Type.CONST, Type.RIGHT_PAREN));
          parsingParams = true;
        }
        else if (current.type() == Type.COMMA) {
          expected.clear();
          expected.addAll(Arrays.asList(Type.CONST, Type.IDENT));
        }
        else if (current.type() == Type.CONST) {
          currentParamIsConstant = true;
          expected.clear();
          expected.add(Type.IDENT);
        }
        else if (current.type() == Type.RIGHT_PAREN) {
          expected.clear();
          expected.addAll(Arrays.asList(Type.OPEN_SCOPE));
        }
        else if (current.type() == Type.OPEN_SCOPE) {
          bodyTokens.addAll(gatherBlock(iterator, current.lineNumber()));
          bodyTokens.remove(bodyTokens.size()-1);
          
          for(Token b: bodyTokens) {
            System.out.println("BODY: "+b);
          }
          
          if (!bodyTokens.isEmpty()) {
            FunctionBodyFormatter bodyFormatter = new FunctionBodyFormatter(bodyTokens);
            bodyFormatter.formFunction(functionBuilder);
          }

          expected.clear();
          expected.add(Type.NO_EXPECT);
          break;
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content()+"'",current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Missing tokens when parsing function: '"+functionBuilder.getName()+"' : "+expected);
    }
    
    functionBuilder.setAsConstructor(isConstructor);
    return functionBuilder.build();
  }
  
  private ClassStruct parseClassDeclaration(ListIterator<Token> iterator) {
    Token name = null;
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.IDENT));
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      System.out.println("---CURRENT "+current);
      if (expected.contains(current.type())) {
        if(current.type() == Type.IDENT) {
          name = current;
          expected.clear();
          expected.add(Type.OPEN_SCOPE);
        }
        else if (current.type() == Type.OPEN_SCOPE) {
          expected.clear();
          expected.add(Type.NO_EXPECT);
          break;
        }
      }
      else {
        throw new ParseError("Misplaced token '"+current.content()+"' ",current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Missing tokens when parsing class: '"+name.content()+"' : "+expected);
    }
    
    List<Token> body = gatherBlock(iterator, current.lineNumber());
    body.remove(body.size()-1);
    
    if (body.isEmpty()) {
      return new ClassStruct(name);
    }
    else {
      ClassStruct struct = new ClassStruct(name);
      iterator = body.listIterator();
      expected = new ArrayList<>(Arrays.asList(Type.LET, Type.FUNCTION, Type.IDENT));
      while (iterator.hasNext()) {
        current = iterator.next();
        System.out.println("---CLASS FORM: "+current);
        if (expected.contains(current.type())) {
          if (current.type() == Type.LET) {
            ArrayList<Token> varDecTokens = new ArrayList<>();
            varDecTokens.add(current);
            varDecTokens.addAll(gatherStatement(iterator, current.lineNumber()));
            varDecTokens.remove(varDecTokens.size() - 1);
                      
            if (varDecTokens.isEmpty() == false) {
              VariableFormatter varFormatter = new VariableFormatter(varDecTokens);
              
              if (!struct.addVariable(varFormatter.formVariable())) {
                throw new RuntimeException("Duplicate variable definition at line "+current.lineNumber());
              }
              
              expected.clear();
              expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.IDENT, Type.NO_EXPECT));
            }
          }
          else if (current.type() == Type.FUNCTION) {
            Function function = parseFunction(iterator, false);
            
            /*
             * Check is there's already a function with the same name
             */
            if(!struct.addFunction(function)) {
              throw new RuntimeException("Duplicate function definition at line "+current.lineNumber());
            }
            
            expected.clear();
            expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.IDENT, Type.NO_EXPECT));
          }
          else if (current.type() == Type.IDENT) {
            if (current.content().equals(name.content())) {
              iterator.previous(); //rewind iterator for parseFunction to properly parse
              
              Function function = parseFunction(iterator, true);
              
              /*
               * Check is there's already a function with the same name
               */
              System.out.println("****CONSTRUCTOR "+current+" || "+function.argAmount());
              if(!struct.addFunction(function)) {
                throw new ParseError("Duplicate constructor definition of class '"+
                                      function.getName().content()+"'",
                                      current.lineNumber());
              }
            }
            else {
              throw new ParseError("Invalid constructor definition",current.lineNumber());
            }
            
            expected.clear();
            expected.addAll(Arrays.asList(Type.LET, Type.FUNCTION, Type.IDENT, Type.NO_EXPECT));
          }
        }
        else {
          throw new ParseError("Misplaced token '"+current.content()+"'",current.lineNumber());
        }
      }
      
      System.out.println("---CLASS PRINT: ");
      for(Function function: struct.getFunctions()) {
        System.out.println("FUNC: "+function.getName()+" || "+function.isConstructor());
      }
      for(Variable variable: struct.getVariables()) {
        System.out.println("VAR: "+variable.getName());
      }
      System.out.println("----CLASS PRINT END-----");
      return struct;
    }
  }
  
  /**
   * Gathers all the tokens between two curly braces
   * @param iterator - the ListIterator to use for traversing the list of tokens
   *        Note - the iterator must be PAST the initial opening curly brace.
   * @param lineNumber - in the case of an empty block that may potentially be
   *                    not closed, this lineNumber will be used to report the 
   *                    location of error.
   *                    
   *        In the case of a non-empty, unclosed block, then the most recently ready Token's line
   *        will be used to report the location of the error
   * @return the list of all tokens in between two curly braces, along
   *         with any other nested blocks.
   *         
   * @throws RuntimeException - if the block is missing a closing curly brace
   */
  public static List<Token> gatherBlock(ListIterator<Token> iterator, int lineNumber){
    List<Token> blockContents = new ArrayList<>();
    
    boolean closureReached = false;
    
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      blockContents.add(current);
      if (current.type() == Type.CLOSE_SCOPE) {
        closureReached = true;
        break;
      }
      else if (current.type() == Type.OPEN_SCOPE) {
        blockContents.addAll(gatherBlock(iterator, current.lineNumber()));
      }
    }
    
    if (closureReached == false) {
      if (current == null) {
        throw new ParseError("Missing closing brace",lineNumber);
      }
      throw new ParseError("Missing closing brace",lineNumber);
    }
    
    return blockContents;
  }
  
  /**
   * Gathers all tokens in between an opening and closing parenthesis
   * @param iterator - the ListIterator to use for traversing the list of tokens
   *        Note - the iterator must be PAST the initial opening parenthesis.
   *        
   * @param lineNumber - in the case of an empty grouping that may potentially be
   *                    not closed, this lineNumber will be used to report the 
   *                    location of error.
   *                    
   *        In the case of a non-empty, unclosed grouping, then the most recently ready Token's line
   *        will be used to report the location of the error
   * @return the list of all tokens in between an opening and closing parenthesis, along
   *         with any other nested parenthesis groupings.
   *         
   * @throws RuntimeException - if the block is missing a closing parenthesis
   */
  public static List<Token> gatherEnclosingParanthesis(ListIterator<Token> iterator, int lineNumber){
    ArrayList<Token> tokens = new ArrayList<>();
    boolean closureReached = false;
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      tokens.add(current);
      if (current.type() == Type.RIGHT_PAREN) {
        closureReached = true;
        break;
      }
      else if (current.type() == Type.LEFT_PAREN) {
        tokens.addAll(gatherEnclosingParanthesis(iterator, current.lineNumber()));
      }
    }
    
    if (closureReached == false) {
      if (current == null) {
        throw new RuntimeException("Missing ')' in line "+lineNumber);
      }
      throw new RuntimeException("Missing ')' in line "+current.lineNumber());
    }
    
    return tokens;
  }
  
  /**
   * Gathers all tokens prior to a semicolon.
   * @param iterator - the ListIterator to use for traversing the list of tokens
   *        Note - the iterator must at the exact start of the statement
   * @param lineNumber - in the case of an empty statement that may potentially be
   *                    not closed, this lineNumber will be used to report the 
   *                    location of error.
   *                    
   *        In the case of a non-empty, unterminated statement, then the most recently ready Token's line
   *        will be used to report the location of the error
   * 
   * @return a List of Tokens that represents a statement
   * @throws RuntimeException - if no semicolon is reached
   */
  public static List<Token> gatherStatement(ListIterator<Token> iterator, int lineNumber){
    ArrayList<Token> tokens = new ArrayList<>();
    
    boolean semiColonReached = false;
    Token current = null;
    while (iterator.hasNext()) {
      current = iterator.next();
      tokens.add(current);
      if (current.type() == Type.STATE_END) {
        semiColonReached = true;
        break;
      }
    }
    
    if (semiColonReached == false) {
      if (current == null) {
        throw new RuntimeException("Missing semicolon in line "+lineNumber);
      }
      throw new RuntimeException("Missing semicolon in line "+current.lineNumber());
    }
    
    return tokens;
  }
}
