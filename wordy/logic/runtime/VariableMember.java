package wordy.logic.runtime;

import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.runtime.types.TypeInstance;
import wordy.logic.runtime.types.ValType;

public class VariableMember extends Member{

  protected ASTNode expr;
  protected Object value;
  protected boolean isConstant;
  protected ValType type; 
  
  public VariableMember(String name, boolean isConstant) {
    this(name, null, isConstant);
  }
  
  public VariableMember(String name, ASTNode expr, boolean isConstant) {
    super(name);
    this.expr = expr;
    this.isConstant = isConstant;
  }

  public void setValue(Object value, ValType type) {
    this.value = value;
    this.type = type;
  }
  
  public ValType getType() {
    return type;
  }
  
  public Object getValue() {
    return value;
  }
  
  public ASTNode getExpr() {
    return expr;
  }
  
  public boolean isConstant() {
    return isConstant;
  }
  
  public VariableMember clone() {
    VariableMember member = new VariableMember(getName(), isConstant);
    member.value = this.value;
    member.expr = this.expr;
    return member;
  }
  
  public String toString() {
    return "VARIABLE: "+getName();
  }
  
  @Override
  public boolean isSettable() {
    if (isConstant) {
      return false;
    }
    return true;
  }

  @Override
  public boolean isCallable() {
    return false;
  }
  
  
  
}
