package wordy.logic.runtime.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
public class Instance {
  protected TypeDefinition definition;
  protected Map<String, VariableMember> instanceVars;
  
  public Instance(TypeDefinition baseClass) {
    this.definition = baseClass;
    instanceVars = new LinkedHashMap<>();
    
    VariableMember thisVar = new VariableMember("this", true);
    thisVar.setValue(this, baseClass.type);
    instanceVars.put(thisVar.getName(), thisVar);
  }
  
  public void copyInstanceVars() {
    Collection<VariableMember> values = definition.getVariables().values();
    
    for(VariableMember member : values) {
      instanceVars.put(member.getName(), member.clone());
    }
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
