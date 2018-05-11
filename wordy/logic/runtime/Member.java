package wordy.logic.runtime;

/**
 * Represents a file member - be it a file variable or a function.
 * 
 * Every file member has a name to be identified with, and either
 * has the property of being "settable" - meaning a value can be associated
 * with the member - or "callable" - meaning the member can be invoked
 * for execution.
 * 
 * @author Jose Guaro
 *
 */
public abstract class Member {
  
  private final String name;
  
  /**
   * Constructs a Member
   * @param name - the String name to be associated with the Member
   */
  public Member(String name) {
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
