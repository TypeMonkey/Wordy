package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents a node meant to be placed within an abstract syntax tree
 * @author Jose Guaro
 *
 */
public abstract class ASTNode {
  
  public enum NodeType{
    CONSTANT,
    LITERAL,
    FUNC_CALL,
    UNARY,
    OPERATOR,
    IDENTIFIER,
    BLOCK_HEADER,
    FUNC_DEC,
    MEM_ACCESS; //aka, the dot operator
  }
    
  protected final NodeType type;
  protected final Token [] tokens;
  
  /**
   * Constructs an ASTNode
   * @param type - the NodeType of the Node
   * @param tokens - the Tokens composing this Node
   */
  public ASTNode(NodeType type, Token ... tokens) {
    this.type = type;
    this.tokens = tokens;
  }
  
  public abstract void visit(NodeVisitor visitor);
  
  /**
   * Gets the tokens composing this node
   * @return the tokens composing this node
   */
  public Token [] tokens() {
    return tokens;
  }
  
  /**
   * Gets the NodeType of this node
   * @return the NodeType of this node
   */
  public NodeType nodeType() {
    return type;
  }
  
}
