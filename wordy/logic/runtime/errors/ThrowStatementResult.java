package wordy.logic.runtime.errors;

import wordy.logic.runtime.components.Instance;

/**
 * Meant to be thrown when the last statement of a function's execution is a 
 * throw statement.
 * 
 * 
 * 
 * @author Jose Guaro
 *
 */
public class ThrowStatementResult extends RuntimeException{
  
  private Instance throwInstance;
  
  public ThrowStatementResult(Instance throwInstance) {
    this.throwInstance = throwInstance;
  }
  
  public Instance getThrowInstance() {
    return throwInstance;
  }
  
}
