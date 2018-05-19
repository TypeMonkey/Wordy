package wordy.logic.runtime.components;

import java.util.HashMap;
import java.util.Map;

import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.types.JavaClassDefinition;

/**
 * Represents the instance of a Java class
 * @author Jose Guaro
 *
 */
public class JavaInstance extends Instance{
  
  private Object object;
  private boolean isStaticRep;
  
  protected JavaInstance(Object object, JavaClassDefinition classDefinition, boolean isStaticRep) {
    super(classDefinition);
    instanceVars.putAll(definition.getVariables());
    this.object = object;
    this.isStaticRep = isStaticRep;
  }
  
  public boolean equality(Instance instance) {
    return object == instance;
  }
  
  public VariableMember retrieveVariable(String memberName) {
    JavaVariableMember instance = (JavaVariableMember) instanceVars.get(memberName);
    if (instance == null) {
      return instance;
    }
    instance.setTarget(object);
    return instance;
  }
  
  public Map<String , VariableMember> varMap() {
    return new HashMap<>(instanceVars);
  }
  
  public boolean isStaticRep() {
    return isStaticRep;
  }
  
  public Object getInstance() {
    return object;
  }
  
  public String toString() {
    if (object == null) {
      return "null";
    }
    return object.toString();
  }
  
  public static JavaInstance createStaticInstance(Class<?> respClass) {
    
    JavaClassDefinition definition = JavaClassDefinition.defineClass(respClass);
    if (definition.getStaticRep() != null) {
      return definition.getStaticRep();
    }
    
    JavaInstance instance = new JavaInstance(respClass.getSimpleName(), definition, true);
    for(VariableMember member : instance.instanceVars.values()) {
      JavaVariableMember javaVar = (JavaVariableMember) member;
      if (!javaVar.isStatic()) {
        instance.instanceVars.remove(javaVar.getName());
      }
    }
    
    definition.setStaticRep(instance);
    
    return instance;
  }
  
  public static JavaInstance wrapInstance(Object instance) {
    if (instance instanceof Instance) {
      throw new IllegalArgumentException("The provided object wasn't a Java object");
    }
    else {
      
      if (instance == null) {
        return getNullRep();
      }
      
      JavaClassDefinition definition = JavaClassDefinition.defineClass(instance.getClass());
      return new JavaInstance(instance, definition, false);
    }
  }
  
  public static JavaInstance getNullRep() {
    return new JavaInstance(null, JavaClassDefinition.defineClass(Object.class), false);
  }
}
