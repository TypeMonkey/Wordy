package wordy.logic.runtime;

import wordy.logic.runtime.types.Instance;
import wordy.logic.runtime.types.TypeDefinition;

public class FileInstance extends Instance{

  public FileInstance(RuntimeFile baseClass) {
    super(baseClass);
    instanceVars = baseClass.getVariables();
  }

  
}
