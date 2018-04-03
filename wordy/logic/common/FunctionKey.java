package wordy.logic.common;

import java.util.Objects;

/**
 * A handy, purely "data" class to use as keys when
 * mapping functions to a map
 * @author Jose Guaro
 *
 */
public class FunctionKey {
  
  public final String name;
  public final int argAmnt;
  
  public FunctionKey(String name, int argAmnt) {
    this.name = name;
    this.argAmnt = argAmnt;
  }
  
  public boolean equals(Object object) {
    if (object instanceof FunctionKey) {
      FunctionKey key = (FunctionKey) object;
      return this.name.equals(key.name) && this.argAmnt == key.argAmnt;
    }
    return false;
  }
  
  public int hashCode() {
    return Objects.hash(name, argAmnt);
  }
  
  public String toString() {
    return "F-KEY: "+name+" | "+argAmnt;
  }
}
