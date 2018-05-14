package wordy.logic.runtime.components;

/**
 * Represents a component of the runtime environment - be it a variable, function, constant or source file.
 * 
 * Every component the property of being "settable" - meaning a value can be associated
 * with the member - or "callable" - meaning the member can be invoked
 * for execution.
 * 
 * The exception to this are constants, which are purely values and cannot be set, called and cannot
 * be identified with a name
 * 
 * @author Jose Guaro
 *
 */
public abstract class Component {
  
  protected final String name;
  
  /**
   * Constructs a Member
   */
  public Component() {
    this(null);
  }
  
  /**
   * Constructs a Member
   * @param name - the String name to be associated with the Member
   */
  public Component(String name) {
    this.name = name;
  }
  
  /**
   * Returns the name of this Member
   * @return the name of this Member
   */
  public String getName() {
    return name;
  }
  
  /**
   * Checks if this member's value can be changed
   * @return true if this member's value can be changed,
   *         false if else
   */
  public abstract boolean isSettable();
  
  /**
   * Check if this member can be called to execution
   * @return true if this member can be called,
   *         false if else
   */
  public abstract boolean isCallable();
  
}
