package wordy.logic.compile.structure;

import wordy.logic.compile.nodes.ASTNode;

/**
 * Represents and if/else-if/else block.
 * @author Jose Guaro
 *
 */
public class IfBlock extends StatementBlock{
  
  private Statement condition;
  private boolean isElseIf;
    
  /**
   * Constructs an IfBlock
   * @param condition - the condition of the if statement
   * @param isElseIf - true if this block represents an else-if / else block
   * 
   * Note: If the this block is actually an else-block, then condition should be null
   */
  public IfBlock(Statement condition, boolean isElseIf) {
    super(BlockType.IF);
    this.condition = condition;
    this.isElseIf = isElseIf;
  }
  
  /**
   * Sets the status of this if-block to an else-if or else
   * @param elseIf - true if this if-block represents an else-if or else block
   */
  public void setAsElseIf(boolean elseIf) {
    this.isElseIf = elseIf;
  }
  
  /**
   * Checks if this IfBlock is an else/else-if block
   * @return true if this IfBlock is actually an else/else-if block,
   *         false if this IfBlock is just a regular if block
   */
  public boolean isElseIf() {
    return isElseIf;
  }
  
  /**
   * Returns the condition of this if block
   * @return the Statement representing the condition of this if block
   *         or null if this if block doesn't have one
   */
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
