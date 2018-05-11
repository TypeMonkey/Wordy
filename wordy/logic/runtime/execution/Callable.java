package wordy.logic.runtime.execution;

import wordy.logic.runtime.Constant;
import wordy.logic.runtime.Member;
import wordy.logic.runtime.RuntimeTable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.TypeInstance;

/**
 * Represents a callable file member (or a member that can be invoked)
 * @author Jose Guaro
 *
 */
public abstract class Callable extends Member{
  
  protected int argAmnt;
  
  /**
   * Constructs a Callable
   * @param name - the name of this member
   */
  public Callable(String name, int argRequired) {
    super(name);
    this.argAmnt = argRequired;
  }
  
  /**
   * Invokes this member, passing the given arguments
   * @param visitor - the NodeVisitor to use to visit the nodes of this call
   * @param args - the arguments to this call
   */
  public abstract Constant call(GenVisitor visitor, RuntimeTable executor, Constant ... args);
  
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
