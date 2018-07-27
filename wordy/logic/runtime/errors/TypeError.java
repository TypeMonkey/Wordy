package wordy.logic.runtime.errors;

public class TypeError extends RuntimeException{
  
  public TypeError(String message, int lineNumber, String filename) {
    super("Runtime Error! "+message+" at line "+lineNumber+" , "+filename);
  }
  
}
