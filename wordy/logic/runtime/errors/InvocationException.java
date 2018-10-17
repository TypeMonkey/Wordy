package wordy.logic.runtime.errors;

import java.util.Stack;

import com.sun.org.apache.regexp.internal.recompile;

import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.GenVisitor;

/**
 * Represents a runtime exception (checked or unchecked) thrown by a any part of a Wordy program
 * 
 * @author Jose Guaro
 *
 */
public class InvocationException extends Exception{
  
  private Instance actualInstance;
  private WordyRuntime runtime;
  private RuntimeTable table;
  private FileInstance file;
  private Stack<TraceElement> stackTrace;
  
  /**
   * Constructs an InvocationException
   * 
   * After constructing an InvocationException, you should also invoke registerTrace()
   * right after as to register the first occurrence of this exception
   * 
   * @param instance - the Throwable Instance (wrapped) that was thrown at runtime
   * @param runtime - the WordyRuntime executing the current code
   * @param table - the RuntimeTable being used at the moment of this exception
   * @param file - the FileInstance at which this exception occurred
   */
  public InvocationException(Instance instance, WordyRuntime runtime, RuntimeTable table, FileInstance file) {
    this.actualInstance = instance;
    this.runtime = runtime;
    this.table = table;
    this.file = file;
    this.stackTrace = new Stack<>();
  }  
  
  public Instance getThrowInstance() {
    return actualInstance;
  }
  
  public void registerTrace(String fileName, int lineNumber) {
    //System.out.println("-----TRACE: AT: "+ fileName+" . "+lineNumber);
    stackTrace.push(new TraceElement(fileName, lineNumber));
  }
  
  public void registerCause(InvocationException exception) {
    //System.out.println("-----TRACE: EXCEPTION: "+exception.actualInstance.getName());
    stackTrace.push(new ExceptionTraceElement(exception));
  }
  
  /**
   * FROM java.lang.Thorwable documentation
   * Provides programmatic access to the stack trace information printed by printStackTrace().
   * 
   * NOTE: This method will return the wrong StackTraceElements when describing the trace of a Wordy Exception.
   * To get the stack trace of an exception in Wordy code, use getWordyTrace();
   */
  public StackTraceElement[] getStackTrace() {
    return super.getStackTrace();
  }
  
  public TraceElement[] getWordyTrace() {
    return stackTrace.toArray(new TraceElement[stackTrace.size()]);
  }
  
  /**
   * Prints the stack trace to the standard error stream
   */
  public void printStackTrace() {    
    System.err.println(actualInstance.getDefinition().getName()+" : "+getMessage());
    for(TraceElement element : stackTrace) {
      System.err.println("  "+element);
    }
  }
  
  public String getMessage() {
    VariableMember variableMember = actualInstance.retrieveVariable("message");
    return variableMember.getValue().toString();
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
  
  
  public static class ExceptionTraceElement extends TraceElement{
    
    private final InvocationException exception;
    
    public ExceptionTraceElement(InvocationException exception) {
      super(exception.getWordyTrace()[0].fileName, 
            exception.getWordyTrace()[0].lineNumber);
      this.exception = exception;
    }
    
    public String toString() {
      String fullMess = exception.actualInstance.getDefinition().getName()+" : "+exception.getMessage();
      for(TraceElement element : exception.stackTrace) {
        System.err.println(" "+element);
      }
      return fullMess;
    }
    
  }
}
