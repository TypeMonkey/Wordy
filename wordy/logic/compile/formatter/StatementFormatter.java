package wordy.logic.compile.formatter;

import java.util.List;

import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;
import wordy.logic.compile.errors.ParseError;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.parser.Parser;
import wordy.logic.compile.structure.Statement;

/**
 * Formats a sequence of Tokens that are terminated by
 * a semicolon, or parenthesis
 * @author Jose Guaro
 *
 */
public class StatementFormatter {
  
  private List<Token> tokens;
  
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
        return new Statement(null, true,false, false);
      }
      else {
        Parser parser = new Parser(tokens);
        return new Statement(parser.parse(), true, false, false);
      }
    }
    else if (tokens.get(0).type() == Type.BREAK) {
      if (tokens.size() > 1) {
        throw new ParseError("Invalid break statement", tokens.get(0).lineNumber());
      }
      else {
        return new Statement(new IdentifierNode(tokens.get(0)), false, true, false);
      }
    }
    else if (tokens.get(0).type() == Type.CONTINUE) {
      if (tokens.size() > 1) {
        throw new ParseError("Invalid continue statement", tokens.get(0).lineNumber());
      }
      else {
        return new Statement(new IdentifierNode(tokens.get(0)), false, false, true);
      }
    }
    else {
      if (tokens.size() == 1) {
        throw new ParseError("Invalid statement", tokens.get(0).lineNumber());
      }
      Parser parser = new Parser(tokens);
      return new Statement(parser.parse(), false, false, false);
    }
  }
}
