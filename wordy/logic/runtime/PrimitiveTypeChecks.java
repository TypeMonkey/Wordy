package wordy.logic.runtime;

/**
 * A convenient class of a collection of static methods
 * to do type checks with primitive types and there respective wrapper classes
 * @author Jose Guaro
 *
 */
public class PrimitiveTypeChecks {
  
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
  
}
