package wordy.logic.runtime;

import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.types.ValType;

public abstract class SystemFunction extends Callable{
  
  private ValType returnType;
  private boolean isAConstructor;
  
  public SystemFunction(String name, int argAmount, ValType returnType) {
    super(name, argAmount);
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
