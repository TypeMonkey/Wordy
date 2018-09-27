package wordy.logic.compile.nodes;
import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents a function/method call
 * 
 * @author Jose Guaro
 *
 */
public class MethodCallNode extends ASTNode{

  private ASTNode [] arguments;
  private ASTNode calle;
  
  /**
   * Constructs a MethodCallNode
   * @param calle - the ASTNode that follows this call
   * @param paren - the right most closing parenthesis of a call
   * @param name - name of the method/function being called
   * @param arguments - arguments for this call
   */
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
  public void accept(NodeVisitor visitor){
    visitor.visit(this);
  }
}
