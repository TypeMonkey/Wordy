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
  
  public abstract void accept(NodeVisitor visitor);
  
  /**
   * Returns the Token that symbolizes the location of this node on the source code
   * 
   * For example, let's say we have a MethodCallNode that represents
   * the function call: 
   * 
   * print("hello"); 
   * 
   * A Token that makes up this MethodCallNode is the Identifier 'print'. 
   * 
   * We can set this Token to be the location Token so that when an event occurs at that
   * Node, we can refer to this Token's line number for reference.
   * 
   * This is useful for parsing and runtime errors.
   *  
   * 
   * @return the Token that symbolizes the location of this node on the source code
   */
  public Token locationToken() {
    return tokens[0];
  }
  
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
