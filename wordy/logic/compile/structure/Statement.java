package wordy.logic.compile.structure;

import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;

public class Statement {
  
  public enum StatementDescription{
    REGULAR,
    VAR_DEC,
    BLOCK,
    BREAK,
    CONTINUE,
    RETURN, 
    THROW;
  }
  
  protected ASTNode expression;
  protected StatementDescription description;
  
  protected Statement(StatementDescription description) {
    this(null, description);
  }
  
  public Statement(ASTNode expression, StatementDescription description) {
    this.expression = expression;
    this.description = description;
  }
    
  public ASTNode getExpression() {
    return expression;
  }
  
  public StatementDescription getDescription() {
    return description;
  }
  
  public String toString() {
    String x = "";
    for(Token token: expression.tokens()) {
      x += token.content()+ " ";
    }
    return x;
  }
}
