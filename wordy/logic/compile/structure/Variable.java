package wordy.logic.compile.structure;

import wordy.logic.compile.Token;
import wordy.logic.compile.nodes.ASTNode;

public class Variable extends Statement{
  
  private Token name;
  private boolean isConstant;
    
  public Variable(Token name, boolean isConstant) {
    this.name = name;
    isVarDec = true;
    this.isConstant = isConstant;
  }
  
  public boolean equals(Object object) {
    if (object instanceof Variable) {
      Variable variable = (Variable) object;
      return variable.getName().content().equals(this.name.content());
    }
    return false;
  }
  
  public boolean isConstant() {
    return isConstant;
  }
  
  public void setAssignment(ASTNode expression) {
    this.expression = expression;
  }

  public Token getName() {
    return name;
  }
  
  public String toString() {
    return name.content();
  }
  
}
