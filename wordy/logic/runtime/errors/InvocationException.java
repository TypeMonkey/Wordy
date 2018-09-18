package wordy.logic.runtime.errors;

import java.util.Stack;

import com.sun.org.apache.regexp.internal.recompile;

import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.execution.Callable;

/**
 * Represents a runtime exception (checked or unchecked) thrown by a any part of a Wordy program
 * 
 * @author Jose Guaro
 *
 */
public class InvocationException extends RuntimeException{
  
  
  private Instance actualInstance;
  private Stack<TraceElement> stackTrace;
  
  /**
   * Constructs an InvocationException
   * @param instance - the Throwable Instance (wrapped) that was thrown at runtime
   */
  public InvocationException(Instance instance) {
    this.actualInstance = instance;
    this.stackTrace = new Stack<>();
  }  
  
  public Instance getThrowInstance() {
    return actualInstance;
  }
  
  /**
   * Prints the stack trace to the standard error stream
   */
  public void printStackTrace() {
    System.err.println(actualInstance.getDefinition().getName()+" : ");
  }
  
  public static class TraceElement{
    public final String fileName; //can't be null. ".rhex" suffix required
    public final int lineNumber;
     
    public TraceElement(String fileName, int lineNumber) {
      this.fileName = fileName;
      this.lineNumber = lineNumber;
    }
    
    public String toString() {
      return "at "+fileName+":"+lineNumber;
    }
    
  }
}
