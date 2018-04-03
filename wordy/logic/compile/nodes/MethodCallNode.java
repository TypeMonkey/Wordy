package wordy.logic.compile.nodes;
import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

public class MethodCallNode extends ASTNode{

  private ASTNode [] arguments;
  private ASTNode calle;
  
  public MethodCallNode(ASTNode calle, Token paren, Token name, ASTNode [] arguments) {
    super(NodeType.FUNC_CALL, name, paren);
    this.calle = calle;
    this.arguments = arguments;
  }
  
  public Token getName() {
    return tokens[0];
  }
  
  public ASTNode getCallee() {
    return calle;
  }
  
  public ASTNode [] arguments() {
    return arguments;
  }

  @Override
  public void visit(NodeVisitor visitor) {
    visitor.visit(this);
  }
}
