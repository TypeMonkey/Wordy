package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * A node that represents a binary operator.
 * 
 * A binary operator is an operator that uses two operands
 * to produce a result.
 * 
 * Addition, Subtraction, Multiplication, Modulus, and Division
 * are the most well known binary operators, but since Wordy
 * supports booleans and assignments, Equals and any 
 * quantifying comparisons are also binary operators.
 * 
 * @author Jose Guaro
 *
 */
public class BinaryOpNode extends ASTNode{
  
  private ASTNode leftOperand;
  private ASTNode rightOperand;
  
  /**
   * Constructs a BinaryOpNode
   * @param op - the operator this node is representing
   * @param left - the left operand of this operator
   * @param right - the right operand of this operator
   */
  public BinaryOpNode(Token op, ASTNode left, ASTNode right) {
    super(NodeType.OPERATOR, op);
    this.leftOperand = left;
    this.rightOperand = right;
  }
  
  public String getOperator() {
    return tokens[0].content();
  }
  
  public ASTNode getLeftOperand() {
    return leftOperand;
  }
  
  public ASTNode getRightOperand() {
    return rightOperand;
  }

  @Override
  public void visit(NodeVisitor visitor) {
    visitor.visit(this);
  }
}
