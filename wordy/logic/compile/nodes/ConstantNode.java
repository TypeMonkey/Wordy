package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents a number constant, be it
 * decimal or integer values.
 * @author Jose Guaro
 *
 */
public class ConstantNode extends ASTNode{
  
  public ConstantNode(Token value) {
    super(NodeType.CONSTANT, value);
  }
  
  public String getValue() {
    return tokens[0].content();
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
