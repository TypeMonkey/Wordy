package wordy.logic.runtime.execution;

import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.types.TypeDefinition;

public abstract class EmbeddedFunction extends Callable{
  
  private TypeDefinition returnType;
  private boolean isAConstructor;
  
  public EmbeddedFunction(String name, int argAmount, TypeDefinition returnType) {
    super(name, argAmount, null);
    this.returnType = returnType;
  }
  
  public boolean argumentsCompatible(Instance ... args) {
    return argAmnt == args.length;
  }

  
  public void setAsConstructor(boolean constr) {
    isAConstructor = constr;
  }
    
  public TypeDefinition getReturnType() {
    return returnType;
  }
  
  public boolean isAConstructor() {
    return isAConstructor;
  }
}
