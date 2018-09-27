package wordy.logic.runtime.errors;

/**
 * Represents a fatal error/exception that has occurred in the internal Wordy execution environment.
 * 
 * Examples of such an exception would be type errors, unfound object members, unfound Java classes, etc.
 * 
 * @author Jose Guaro
 *
 */
public class FatalInternalException extends RuntimeException{
  
  private String fileName;
  private int lineNumber;
  
  public FatalInternalException(String fileName, int lineNumber, String message) {
    super(message+" , occured at "+fileName+", line "+lineNumber);
    
    this.fileName = fileName;
    this.lineNumber = lineNumber;
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public int getLineNumber() {
    return lineNumber;
  }
  
}
