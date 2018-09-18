package wordy.logic.runtime;

import wordy.logic.runtime.components.JavaInstance;

/**
 * A convenient class made of a collection of static methods
 * to do type checks with primitive types and there respective wrapper classes
 * @author Jose Guaro
 *
 */
public class TypeChecks {
  
  public static boolean isCompatible(Class<?> acceptor, Class<?> value) {
    if ( (acceptor == Integer.class || acceptor == Integer.TYPE) && 
         (value == Integer.class || value == Integer.TYPE)) {
      return true;
    }
    else if ( (acceptor == Double.class || acceptor == Double.TYPE) && 
        (value == Double.class || value == Double.TYPE)) {
     return true;
    }
    else if ( (acceptor == Boolean.class || acceptor == Boolean.TYPE) && 
        (value == Boolean.class || value == Boolean.TYPE)) {
     return true;
    }
    else if ( (acceptor == Long.class || acceptor == Long.TYPE) && 
        (value == Long.class || value == Long.TYPE)) {
     return true;
    }
    else if ( (acceptor == Character.class || acceptor == Character.TYPE) && 
        (value == Character.class || value == Character.TYPE)) {
     return true;
    }
    else {
      return acceptor.isAssignableFrom(value);
    }
  }
  
  /**
   * Returns the boolean equivalent of the given instance
   * 
   * All non-zero values represent true. (Ex: 5, -1, 9.5, "hello", non-null references)
   * Zero values represent false. (Ex: null character (\0), null references, 0, 0.0)
   * 
   * @param instance - the instance to check
   * @return true if instance doesn't represent a 0.
   *         false if it does
   */
  public static boolean getBooleanEquivalent(Object instance) {
    if ( (instance.getClass() == Integer.class || instance.getClass()  == Integer.TYPE) ) {
      return !instance.equals(0);
    }
    else if ( (instance.getClass() == Double.class || instance.getClass() == Double.TYPE) ) {
      return !instance.equals(0.0);
    }
    else if ( (instance.getClass() == Boolean.class || instance.getClass() == Boolean.TYPE) ) {
      return (boolean) instance;
    }
    else if ( (instance.getClass() == Long.class || instance.getClass() == Long.TYPE) ) {
      return !instance.equals(0);
    }
    else if ( (instance.getClass() == Character.class || instance.getClass() == Character.TYPE) ) {
      return !instance.equals(Character.MIN_VALUE);
    }
    else {
      return instance != null;
    }
  }
}
