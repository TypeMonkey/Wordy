package wordy.logic.runtime.execution;

import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.Component;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;

/**
 * Represents a callable file member (or a member that can be invoked)
 * @author Jose Guaro
 *
 */
public abstract class Callable extends Component{
  
  protected int argAmnt;
  protected WordyRuntime runtime;
  /**
   * Constructs a Callable
   * @param name - the name of this member
   */
  public Callable(String name, int argRequired, WordyRuntime runtime) {
    super(name);
    this.argAmnt = argRequired;
    this.runtime = runtime;
  }
  
  /**
   * Invokes this member, passing the given arguments
   * @param visitor - the NodeVisitor to use to visit the nodes of this call
   * @param args - the arguments to this call
   */
  public abstract Instance call(GenVisitor visitor, RuntimeTable table, Instance ... args);
  
  public final int requiredArgs() {
    return argAmnt;
  }
  
  public abstract boolean isAConstructor();
  
  public final boolean isSettable() {
    return false;
  }
  
  public final boolean isCallable() {
    return true;
  }
  
}
