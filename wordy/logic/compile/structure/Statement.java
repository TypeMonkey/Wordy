package wordy.logic.compile.structure;

import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;

public class Statement {
  
  protected ASTNode expression;
  
  protected boolean isVarDec;
  protected boolean isABlock;
  protected boolean isAReturn;
  protected boolean isABreak;
  protected boolean isAContinue;
  
  protected Statement() {
    this(null, false, false, false);
  }
  
  public Statement(ASTNode expression, boolean isAReturn, boolean isABreak, boolean isAContinue) {
    this.expression = expression;
    this.isAReturn = isAReturn;
    this.isABreak = isABreak;
    this.isAContinue = isAContinue;
  }
    
  public ASTNode getExpression() {
    return expression;
  }
  
  public boolean isAVarDec() {
    return isVarDec;
  }
  
  public boolean isABlock() {
    return isABlock;
  }
  
  public boolean isAReturn() {
    return isAReturn;
  }
  
  public boolean isABreak() {
    return isABreak;
  }
  
  public boolean isAContinue() {
    return isAContinue;
  }
  
  public String toString() {
    String x = "";
    for(Token token: expression.tokens()) {
      x += token.content()+ " ";
    }
    return x;
  }
}
