package wordy.logic.compile.nodes;

import wordy.logic.common.NodeVisitor;
import wordy.logic.compile.Token;

/**
 * Represents the dot operator
 * 
 * For example, let's say we have this class:
 * 
 * class Obie{
 * 
 *  let h = 10;
 *  
 *  Obie(){
 *    //default constructor
 *  }
 * 
 * }
 * 
 * And say we made an object from it:
 * 
 * let myObject = Obie();
 * 
 * myObject.h is represented using this Node. More specifically, "calle" would be 'myObject'
 * and "memberName" would be 'h'
 * 
 * @author Jose Guaro
 *
 */
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
