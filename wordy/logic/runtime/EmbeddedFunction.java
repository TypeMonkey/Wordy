package wordy.logic.runtime;

import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.types.ValType;

public abstract class EmbeddedFunction extends Callable{
  
  private ValType returnType;
  private boolean isAConstructor;
  
  public EmbeddedFunction(String name, int argAmount, ValType returnType) {
    super(name, argAmount, null);
    this.returnType = returnType;
  }
  
  public void setAsConstructor(boolean constr) {
    isAConstructor = constr;
  }
    
  public ValType getReturnType() {
    return returnType;
  }
  
  public boolean isAConstructor() {
    return isAConstructor;
  }
}
