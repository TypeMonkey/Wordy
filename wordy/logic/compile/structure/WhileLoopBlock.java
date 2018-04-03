package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.List;

import wordy.logic.compile.nodes.ASTNode;

public class WhileLoopBlock extends StatementBlock{
  
  private Statement conditional;
  private boolean isDoWhile;

  public WhileLoopBlock(Statement comparison, boolean isDoWhile) {
    super(BlockType.WHILE);
    this.conditional = comparison;
    this.isDoWhile = isDoWhile;
  }
  
  public Statement getCondition() {
    return conditional;
  }
  
  public ASTNode getExpression() {
    return conditional.expression;
  }
  
  public boolean isDoWhile() {
    return isDoWhile;
  }
  
  public Object execute() {
    return null;
  }
  
  public String toString() {
    String x = "LOOP COND: "+conditional+System.lineSeparator();
    x += super.toString();
    return x;
  }
}
