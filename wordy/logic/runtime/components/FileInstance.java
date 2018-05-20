package wordy.logic.runtime.components;

import wordy.logic.runtime.RuntimeFile;

public class FileInstance extends Instance{

  public FileInstance(RuntimeFile baseClass) {
    super(baseClass);
    instanceVars = baseClass.getVariables();
  }

  public String toString() {
    return "Source File: "+name;
  }
  
  public RuntimeFile getDefinition() {
    return (RuntimeFile) definition;
  }
}
