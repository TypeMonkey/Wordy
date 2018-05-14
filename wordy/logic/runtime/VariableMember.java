package wordy.logic.runtime;

import wordy.logic.compile.nodes.ASTNode;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.StackComponent;
import wordy.logic.runtime.types.TypeDefinition;

public class VariableMember extends StackComponent{

  protected ASTNode expr;
  protected Instance value;
  protected boolean isConstant;
  protected TypeDefinition type; 
  
  public VariableMember(String name, boolean isConstant) {
    this(name, null, isConstant);
  }
  
  public VariableMember(String name, ASTNode expr, boolean isConstant) {
    this(name, null, expr, isConstant);
  }

  public VariableMember(String name, Instance value, ASTNode expr, boolean isConstant) {
    super(name);
    this.value = value;
    this.expr = expr;
    this.isConstant = isConstant;
  }
  
  public void setValue(Instance constant) {
    if (isConstant) {
      throw new IllegalStateException("Can't change the value of a constant variable");
    }
    this.value = constant;
    type = constant.getDefinition();
  }
  
  public TypeDefinition getType() {
    return type;
  }
  
  public Instance getValue() {
    return value;
  }
  
  public ASTNode getExpr() {
    return expr;
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
    return isConstant;
  }

  @Override
  public boolean isAnInstance() {
    return false;
  }
  
}
