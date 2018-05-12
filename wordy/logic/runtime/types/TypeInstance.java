package wordy.logic.runtime.types;

import java.util.Collection;

import wordy.logic.runtime.VariableMember;

public class TypeInstance extends Instance{

  public TypeInstance(TypeDefinition baseClass) {
    super(baseClass);
    
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

}
