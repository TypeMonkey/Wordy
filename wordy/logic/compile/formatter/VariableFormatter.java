package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.compile.parser.Parser;
import wordy.logic.compile.structure.Variable;

/**
 * Forms a variable from a list of Tokens
 * @author Jose Guaro
 *
 */
public class VariableFormatter {
  
  private List<Token> tokens;
  
  /**
   * Constructs a VariableFormatter with the list of tokens
   * representing the variable declaration
   * @param tokens - list of Tokens. Shouldn't contain the semicolon
   */
  public VariableFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public Variable formVariable() {    
    ListIterator<Token> iterator = tokens.listIterator();
    
    ArrayList<Type> expected = new ArrayList<>(Arrays.asList(Type.LET));
        
    Token varName = null;
    ASTNode expression = null;
    Token current = null;
    boolean isConstant = false;
    while (iterator.hasNext()) {
      current = iterator.next();
      if (expected.contains(current.type())) {
        if (current.type() == Type.LET) {
          expected.clear();
          expected.addAll(Arrays.asList(Type.IDENT, Type.CONST));
        }
        else if (current.type() == Type.CONST) {
          isConstant = true;
          expected.clear();
          expected.add(Type.IDENT);
        }
        else if (current.type() == Type.IDENT) {
          varName = current;
          
          expected.clear();
          expected.addAll(Arrays.asList(Type.NO_EXPECT, Type.EQUALS));
        }
        else if (current.type() == Type.EQUALS) {
          
          List<Token> assignment = new ArrayList<>();
          
          while (iterator.hasNext()) {
            Token assgnCurrent = iterator.next();
            assignment.add(assgnCurrent);           
          }
          
          /*
           * Is assignment list is empty, then that's not valid syntax
           */
          if (assignment.isEmpty()) {
            throw new ParseError("Invalid assignment expression", current.lineNumber());
          }
          System.out.println("---PARING: "+varName.content()+" | "+assignment);
          expression = new Parser(assignment).parse();        
        }
      }
      else {
        throw new RuntimeException("Misplaced token '"+current.content()+"' in line "+current.lineNumber());
      }
    }
    
    if (expected.contains(Type.NO_EXPECT) == false) {
      throw new RuntimeException("Missing tokens: "+expected+" at line: "+current.lineNumber());
    }
        
    Variable variable = new Variable(varName,isConstant);
    variable.setAssignment(expression);
    
    return variable;
  }
}
