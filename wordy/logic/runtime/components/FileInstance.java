package wordy.logic.runtime.components;

import wordy.logic.runtime.RuntimeFile;
import wordy.logic.runtime.types.TypeDefinition;

public class FileInstance extends Instance{

  public FileInstance(RuntimeFile baseClass) {
    super(baseClass);
    instanceVars = baseClass.getVariables();
  }

  public RuntimeFile getDefinition() {
    return (RuntimeFile) definition;
  }
}
