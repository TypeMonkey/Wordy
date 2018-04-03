package wordy.logic.runtime.types;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.runtime.Constant;
import wordy.logic.runtime.VariableMember;

/**
 * Represents the instance of a TypeDefinition
 * @author Jose Guaro
 *
 */
public class TypeInstance {
  private TypeDefinition definition;
  private Map<String, VariableMember> instanceVars;
  
  public TypeInstance(TypeDefinition baseClass) {
    this.definition = baseClass;
    instanceVars = new LinkedHashMap<>();
  }
  
  public void placeVariable(VariableMember member) {
    instanceVars.put(member.getName(), member);
  }
  
  public VariableMember retrieveVariable(String memberName) {
    return instanceVars.get(memberName);
  }
  
  public TypeDefinition getDefinition() {
    return definition;
  }
  
  public List<VariableMember> getVariables() {
    return new ArrayList<>(instanceVars.values()); 
  }
}
