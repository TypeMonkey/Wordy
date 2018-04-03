package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.List;

import wordy.logic.compile.nodes.ASTNode;

public class IfBlock extends StatementBlock{
  
  private Statement condition;
  private boolean isElseIf;
    
  public IfBlock(Statement condition, boolean isElseIf) {
    super(BlockType.IF);
    this.condition = condition;
    this.isElseIf = isElseIf;
  }
  
  public void setAsElseIf(boolean elseIf) {
    this.isElseIf = elseIf;
  }
  
  public boolean isElseIf() {
    return isElseIf;
  }
  
  public Statement getCondition() {
    return condition;
  }
  
  public ASTNode getExpression() {
    return condition.expression;
  }
  
  public String toString() {
    String x = "CONDITION: "+condition+System.lineSeparator();
    x += super.toString();
    return x;
  }
}
