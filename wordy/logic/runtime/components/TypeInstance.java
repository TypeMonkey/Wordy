package wordy.logic.runtime.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.TypeDefinition;

public class TypeInstance extends Instance{
    
  private TypeInstance(TypeDefinition baseClass, Instance superInstance) {
    super(baseClass);
    
    VariableMember thisVar = new VariableMember("this", this, null, true);
    instanceVars.put(thisVar.getName(), thisVar);
    
    VariableMember superVar = new VariableMember("super", superInstance, null, true);
    instanceVars.put(superVar.getName(), superVar);
  }
  
  private void copyInstanceVars() {
    Collection<VariableMember> values = definition.getVariables().values();
    
    for(VariableMember member : values) {
      instanceVars.put(member.getName(), member.clone());
    }
  }
  
  public VariableMember retrieveVariable(String memberName) {
    if (instanceVars.containsKey(memberName)) {
      return instanceVars.get(memberName);
    }
    
    VariableMember superVar = instanceVars.get("super");
    return superVar.getValue().retrieveVariable(memberName);
  }
  
  public Map<String, VariableMember> declaredVars(){
    VariableMember superVar = instanceVars.get("super");
    
    Set<String> currentKeySet = new HashSet<>(instanceVars.keySet());
    currentKeySet.addAll(new HashSet<>(superVar.getValue().varMap().keySet()));
    
    HashMap<String, VariableMember> allVars = new HashMap<>();
    for(String varName : currentKeySet) {
      if (instanceVars.containsKey(varName)) {
        allVars.put(varName, instanceVars.get(varName));
      }
      else {
        allVars.put(varName, superVar.getValue().retrieveVariable(varName));
      }
    }
    return allVars;
  }

  public static TypeInstance newInstance(TypeDefinition definition, Instance superInstance) {
    TypeInstance instance = new TypeInstance(definition, superInstance);
    instance.copyInstanceVars();
    
    return instance;
  }
}
