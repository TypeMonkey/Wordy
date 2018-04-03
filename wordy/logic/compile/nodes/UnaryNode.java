package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;
import wordy.logic.compile.Token.Type;

/**
 * Represents a unary operator
 * 
 * A unary operator is any operator that operates
 * on a single operand.
 * 
 * Currently, the subtraction and boolean negation (BANG!)
 * symbols are supported
 * 
 * @author Jose Guaro
 *
 */
public class UnaryNode extends ASTNode{
  
  private Type unaryType;
  private ASTNode expr;
  
  public UnaryNode(Token unary, ASTNode expr) {
    super(NodeType.UNARY, unary);
    this.unaryType = unary.type();
    this.expr = expr;
  }
  
  public Type unaryType() {
    return unaryType;
  }
  
  public ASTNode getExpr() {
    return expr;
  }

  @Override
  public void visit(NodeVisitor visitor) {
    visitor.visit(this);
  }
  
  
  
}
