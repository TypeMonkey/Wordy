package wordy.logic.runtime.components;

import java.util.Collection;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.TypeDefinition;

public class TypeInstance extends Instance{

  private TypeInstance(TypeDefinition baseClass) {
    super(baseClass);
    
    VariableMember thisVar = new VariableMember("this", true);
    thisVar.setValue(this);
    instanceVars.put(thisVar.getName(), thisVar);
  }
  
  private void copyInstanceVars() {
    Collection<VariableMember> values = definition.getVariables().values();
    
    for(VariableMember member : values) {
      instanceVars.put(member.getName(), member.clone());
    }
  }

  public static TypeInstance newInstance(TypeDefinition definition) {
    TypeInstance instance = new TypeInstance(definition);
    instance.copyInstanceVars();
    
    return instance;
  }
}
