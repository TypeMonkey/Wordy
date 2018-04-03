package wordy.logic.compile.errors;

public class ParseError extends RuntimeException{
  
  public ParseError(String message, int lineNumber) {
    super(message + " at line "+lineNumber);
  }
  
}
