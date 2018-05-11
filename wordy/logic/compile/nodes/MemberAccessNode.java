package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

public class MemberAccessNode extends ASTNode{

  private ASTNode calle;
  private boolean forFunction;
  
  public MemberAccessNode(ASTNode calle, Token memberName) {
    super(NodeType.MEM_ACCESS, memberName);
    this.calle = calle;
  }
  
  public void setForFunction(boolean forfunc) {
    forFunction = forfunc;
  }
  
  public boolean isForFunction() {
    return forFunction;
  }
  
  public Token getMemberName() {
    return tokens[0];
  }
  
  public ASTNode getCalle() {
    return calle;
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
