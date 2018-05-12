package wordy.logic.runtime;

import wordy.logic.runtime.types.ValType;

public class Constant extends VariableMember{

  public static final Constant VOID = new Constant(ValType.VOID, null);
  
  public Constant(ValType type, Object value) {
    super(null, true);
    this.value = value;
    this.type = type;
    this.isConstant = true;
  }
  
  public void setValue(Object val, ValType type) {
    throw new RuntimeException("Cannot set the value of a constant");
  }
 
  public String toString() {
    return "Constant: "+value;
  }
  
  @Override
  public boolean isSettable() {
    return false;
  }
}
