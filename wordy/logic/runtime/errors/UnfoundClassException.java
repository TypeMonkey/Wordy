package wordy.logic.runtime.errors;

/**
 * An exception that's thrown when a Java class cannot be loaded, or found
 * @author Jose Guaro
 *
 */
public class UnfoundClassException extends FatalInternalException{
  
  public UnfoundClassException(String className, String fileName, int lineNumber) {
    super("Cannot load the Java class "+className, lineNumber, fileName);
  }
  
}
