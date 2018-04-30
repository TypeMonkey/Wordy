package wordy.logic.compile.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Function.FunctionBuilder;

/**
 * Forms a function from a list of tokens that contains a function's contents
 * @author Jose Guaro
 *
 */
public class FunctionBodyFormatter {
  
  private List<Token> tokens;
  
  /**
   * Constructs a FunctionBodyFormatter
   * 
   * @param tokens - the List of Tokens containing the body of this function.
   *            Note: the opening and closing braces of this function must be removed
   */
  public FunctionBodyFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public void formFunction(FunctionBuilder builder) {
    ListIterator<Token> iterator = tokens.listIterator();        
    ArrayList<Token> tempStatement = new ArrayList<>();
        
    Token token = null;
    
    while (iterator.hasNext()) {
      token = iterator.next();
      tempStatement.add(token);
      System.out.println("---FUNC FORM: "+token);
      if (token.type() == Type.STATE_END ) {
        tempStatement.remove(tempStatement.size()-1);
        
        if (tempStatement.isEmpty()) {
          throw new ParseError("Empty statement", token.lineNumber());
        }
        
        StatementFormatter statementFormatter = new StatementFormatter(tempStatement);
        builder.addStatement(statementFormatter.formatStatements(), false);
        
        System.out.println("---FORM: STATE: "+tempStatement);
        
        tempStatement = new ArrayList<>();
      }
      else if (token.type() == Type.BLOCK_SIG) {        
        boolean headerEndReached = false;
        while (iterator.hasNext()) {
          Token header = iterator.next();
          tempStatement.add(header);
          if (header.type() == Type.OPEN_SCOPE) {
            headerEndReached = true;
            break;
          }
        }
        
        if (headerEndReached == false) {
          throw new RuntimeException("Missing '{' for scope declaration at line "+token.lineNumber());
        }
        
        tempStatement.addAll(Formatter.gatherBlock(iterator, token.lineNumber()));
        
        BlockFormatter formatter = new BlockFormatter(tempStatement);
        builder.addStatement(formatter.formBlock(), false);
        
        tempStatement = new ArrayList<>();
      }
      else if (token.type() == Type.OPEN_SCOPE) {
        //these are for general scopes/blocks
        tempStatement.addAll(Formatter.gatherBlock(iterator, token.lineNumber()));
        
        for(Token g: tempStatement) {
          System.out.println("-GENERAL: "+g);
        }
        
        BlockFormatter formatter = new BlockFormatter(tempStatement);
        builder.addStatement(formatter.formBlock(), false);
        
        tempStatement = new ArrayList<>();
      }
    }
    
    //the temp statement isn't empty which means a statement delimiter 
    // - be it a semicolon, or a opening curly brace - hasn't been encountered.
    //This means we have dangling statements
    if (!tempStatement.isEmpty()) {
      throw new ParseError("Dangling statemen or block declaration", token.lineNumber());
    }
  }
}
