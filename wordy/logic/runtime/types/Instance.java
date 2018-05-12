package wordy.logic.runtime.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import wordy.logic.runtime.VariableMember;

/**
 * Represents the instance of a TypeDefinition
 * @author Jose Guaro
 *
 */
public abstract class Instance {
  protected TypeDefinition definition;
  protected Map<String, VariableMember> instanceVars;
  
  public Instance(TypeDefinition baseClass) {
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
}
