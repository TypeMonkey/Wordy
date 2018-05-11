package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents an identifier.
 * 
 * An identifier is used to associate names with
 * functions and variables.
 * 
 * @author Jose Guaro
 *
 */
public class IdentifierNode extends ASTNode{
  
  public IdentifierNode(Token name) {
    super(NodeType.IDENTIFIER, name);
  }
  
  public Token getTokenName() {
    return tokens[0];
  }
  
  public String name() {
    return tokens[0].content();
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }
  
}
