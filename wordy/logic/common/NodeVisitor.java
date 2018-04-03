package wordy.logic.common;

import wordy.logic.compile.nodes.BinaryOpNode;
import wordy.logic.compile.nodes.ConstantNode;
import wordy.logic.compile.nodes.IdentifierNode;
import wordy.logic.compile.nodes.LiteralNode;
import wordy.logic.compile.nodes.MemberAccessNode;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.nodes.UnaryNode;

/**
 * Interface for defining a node visitor
 * @author Jose Guaro
 *
 */
public interface NodeVisitor {
  
  /**
   * For visiting a BinaryOpNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(BinaryOpNode node);
  
  /**
   * For visiting a ConstantNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(ConstantNode node);
  
  /**
   * For visiting an IdentifierNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(IdentifierNode node);
  
  /**
   * For visiting a LiteralNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(LiteralNode node);
  
  /**
   * For visiting a MemberAccessNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(MemberAccessNode node);
  
  /**
   * For visiting a MethodCallNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(MethodCallNode node);
  
  /**
   * For visiting a UnaryNode
   * @param binaryOpNode - the node to visit
   */
  public void visit(UnaryNode node);
}
