package wordy.logic.common;

import java.util.Arrays;
import java.util.Objects;

import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class JavaFunctionKey extends FunctionKey{

  private Class<?> [] argTypes;
  
  public JavaFunctionKey(String name, Class<?> ... fullTypeNames) {
    super(name, fullTypeNames.length);
    this.argTypes = fullTypeNames;
  }

  public boolean equals(Object object) {
    if (object instanceof JavaFunctionKey) {
      JavaFunctionKey key = (JavaFunctionKey) object;
      return super.equals(key) && compatibleTypes(key.argTypes);
    }
    return false;
  }
  
  private boolean compatibleTypes(Class<?> ... args) {
    for(int i = 0 ; i < argTypes.length; i++) {
      if (argTypes[i].isAssignableFrom(Instance.class)) {
        
      }
      if (argTypes[i].isAssignableFrom(args[i]) == false) {
        return false;
      }
    }
    return true;
  }
  
  public int hashCode() {
    return Objects.hash(name, argTypes);
  }
  
  public String toString() {
    return "JF-KEY: "+name+" | "+Arrays.toString(argTypes);
  }
  
  public static JavaFunctionKey spawnKey(String name, Instance ... args) {
    
  }
}
