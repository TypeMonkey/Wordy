package wordy.logic.runtime.errors;

public class UnfoundClassException extends RuntimeException{
  
  public UnfoundClassException(String className, String fileName, int lineNumber) {
    super("Cannot find the class '"+className+"' , at line "+lineNumber+" , in "+fileName);
  }
  
}
