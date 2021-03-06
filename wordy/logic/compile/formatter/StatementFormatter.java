package wordy.logic.compile.formatter;

import java.util.List;

import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.parser.Parser;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Statement.StatementDescription;

/**
 * Formats a sequence of Tokens that are terminated by
 * a semicolon
 * @author Jose Guaro
 *
 */
public class StatementFormatter {
  
  private List<Token> tokens;
  
  /**
   * Constructs a StatementFormatter
   * @param tokens - the List of Tokens to format. 
   */
  public StatementFormatter(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  public Statement formatStatements(){
    if (tokens.get(0).type() == Type.LET) {
      VariableFormatter varFormatter = new VariableFormatter(tokens);
      return varFormatter.formVariable();
    }
    else if (tokens.get(0).type() == Type.RETURN) {
      tokens.remove(0);
      if (tokens.isEmpty()) {
        return new Statement(null, StatementDescription.RETURN);
      }
      else {
        Parser parser = new Parser(tokens);
        return new Statement(parser.parse(), StatementDescription.RETURN);
      }
    }
    else if (tokens.get(0).type() == Type.BREAK) {
      if (tokens.size() > 1) {
        throw new ParseError("Invalid break statement", tokens.get(0).lineNumber());
      }
      else {
        return new Statement(new IdentifierNode(tokens.get(0)),StatementDescription.BREAK);
      }
    }
    else if (tokens.get(0).type() == Type.CONTINUE) {
      if (tokens.size() > 1) {
        throw new ParseError("Invalid continue statement", tokens.get(0).lineNumber());
      }
      else {
        return new Statement(new IdentifierNode(tokens.get(0)), StatementDescription.CONTINUE);
      }
    }
    else {
      StatementDescription description = StatementDescription.REGULAR;
      if (tokens.get(0).content().equals(ReservedSymbols.THROW)) {
        tokens.remove(0);
        description = StatementDescription.THROW;
      }
      
      if (tokens.size() == 1) {
        if (tokens.get(0).type() != Type.IDENT) {
          System.out.println(tokens.get(0).content());
          throw new ParseError("Invalid statement", tokens.get(0).lineNumber());
        }
      }
      
      Parser parser = new Parser(tokens);
      return new Statement(parser.parse(), description);
    }
  }
}
