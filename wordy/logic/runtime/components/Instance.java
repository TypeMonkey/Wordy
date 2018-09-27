package wordy.logic.runtime.components;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.TypeDefinition;

/**
 * Represents the instance of a TypeDefinition
 * @author Jose Guaro
 *
 */
public abstract class Instance extends StackComponent{
  
  protected TypeDefinition definition;
  protected Map<String, VariableMember> instanceVars;
  
  public Instance(TypeDefinition baseClass) {
    super(baseClass.getName());
    this.definition = baseClass;
    instanceVars = new LinkedHashMap<>();
  }

  public VariableMember retrieveVariable(String memberName) {
    return instanceVars.get(memberName);
  }
  
  public TypeDefinition getDefinition() {
    return definition;
  }
  
  public Map<String , VariableMember> varMap() {
    return new HashMap<>(instanceVars);
  }
  
  public final boolean isSettable() {
    return false;
  }
  
  public final boolean isAnInstance() {
    return true;
  }
}
