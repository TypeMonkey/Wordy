package wordy.logic.common;

import java.util.Arrays;
import java.util.Objects;

import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;

public class JavaFunctionKey extends FunctionKey{

  private String [] argTypes;
  
  public JavaFunctionKey(String name, String ... fullTypeNames) {
    super(name, fullTypeNames.length);
    this.argTypes = fullTypeNames;
  }

  public boolean equals(Object object) {
    if (object instanceof JavaFunctionKey) {
      JavaFunctionKey key = (JavaFunctionKey) object;
      return super.equals(key) && Arrays.equals(key.argTypes, argTypes);
    }
    return false;
  }
  
  public int hashCode() {
    return Objects.hash(name, argTypes);
  }
  
  public String toString() {
    return "JF-KEY: "+name+" | "+Arrays.toString(argTypes);
  }
  
  public static JavaFunctionKey spawnKey(String name, Class<?> ... types) {
    String [] typeNames = new String[types.length];
    
    for(int i = 0; i < typeNames.length; i++) {
      typeNames[i] = types[i].getName();
    }
    
    return new JavaFunctionKey(name, typeNames);
  }
  
  public static JavaFunctionKey spawnKey(String name, Object ... instances) {
    String [] typeNames = new String[instances.length];
    
    for(int i = 0; i < typeNames.length; i++) {
      typeNames[i] = instances[i].getClass().getName();
    }
    
    return new JavaFunctionKey(name, typeNames);
  }
}
