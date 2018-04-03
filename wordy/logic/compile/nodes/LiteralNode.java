package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents a string or character literal.
 * 
 * A string literal is any sequence of characters
 * in between two double quotes
 * 
 * A char literal is character in between two
 * single quotes
 * @author Jose Guaro
 *
 */
public class LiteralNode extends ASTNode{

  public LiteralNode(Token literal) {
    super(NodeType.LITERAL, literal);
  }

  public String getLiteralContent() {
    return tokens[0].content();
  }
  
  @Override
  public void visit(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
