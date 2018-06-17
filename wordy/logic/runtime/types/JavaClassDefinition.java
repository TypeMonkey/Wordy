package wordy.logic.runtime.types;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import wordy.logic.common.FunctionKey;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.components.JavaVariableMember;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.JavaCallable;

public class JavaClassDefinition extends TypeDefinition{
  
  /*
  public final static JavaClassDefinition VOID;
  public final static JavaClassDefinition NUMBER ;
  public final static JavaClassDefinition INTEGER;
  public final static JavaClassDefinition DOUBLE;
  public final static JavaClassDefinition BOOLEAN;
  public final static JavaClassDefinition STRING;
  public final static JavaClassDefinition OBJECT;
*/
  
  private static HashMap<Class<?>, JavaClassDefinition> mappedClasses = new HashMap<>();
  
  /*
  static {
    VOID = defineClass(Void.class);
    NUMBER = defineClass(Number.class);
    INTEGER = defineClass(Integer.class);
    DOUBLE = defineClass(Double.class);
    BOOLEAN = defineClass(Boolean.class);
    STRING = defineClass(String.class);
    OBJECT = defineClass(Object.class);
    
    mappedClasses.put(int.class, INTEGER);
    mappedClasses.put(double.class, DOUBLE);
    mappedClasses.put(boolean.class, BOOLEAN);
    mappedClasses.put(Number.class, NUMBER);
    mappedClasses.put(Integer.class, INTEGER);
    mappedClasses.put(Double.class, DOUBLE);
    mappedClasses.put(Boolean.class, BOOLEAN);
    mappedClasses.put(String.class, STRING);
  }
  */
  
  private JavaInstance staticRep;
  private Class<?> respClass;
  private List<JavaClassDefinition> interfaces;
  
  protected JavaClassDefinition(Class<?> respClass) {
    super(respClass.getName());
    this.respClass = respClass;
    interfaces = new ArrayList<>();
    
    if (respClass.equals(Object.class)) {
      parent = null;
    }
  }
  
  public boolean isChildOf(TypeDefinition definition) {
    if (super.isChildOf(definition)) {
      return true;
    }
    
    for(JavaClassDefinition interDef : interfaces) {
      if (interDef.isChildOf(definition)) {
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Object object) {
    if (object instanceof JavaClassDefinition) {
      JavaClassDefinition toCheck = (JavaClassDefinition) object;
      return name.equals(toCheck.name);
    }
    return false;
  }
  
  public void setStaticRep(JavaInstance instance) {
    staticRep = instance;
  }
  
  public boolean isInterface() {
    return respClass.isInterface();
  }
  
  public boolean isAbstract() {
    return Modifier.isAbstract(respClass.getModifiers());
  }
  
  public boolean isFinal() {
    return Modifier.isFinal(respClass.getModifiers());
  }
  
  public String getSimpleName() {
    return respClass.getSimpleName();
  }
  
  public JavaInstance getStaticRep() {
    return staticRep;
  }
  
  public static JavaClassDefinition defineClass(Class<?> respClass) {
    if (mappedClasses.containsKey(respClass)) {
      return mappedClasses.get(respClass);
    }
    else {
      JavaClassDefinition definition = new JavaClassDefinition(respClass);

      for(Constructor<?> constructor: respClass.getConstructors()) {
        JavaCallable callable = new JavaCallable(constructor);
        FunctionKey functionKey = new FunctionKey(callable.getName(), callable.requiredArgs()); 
        if (definition.functions.containsKey(functionKey)) {
          List<Callable> sameFuncKeys = definition.functions.get(functionKey);
          sameFuncKeys.add(callable);
        }
        else {
          definition.functions.put(functionKey, new ArrayList<Callable>(Arrays.asList(callable)));
        }
        
        //adds constructor callable to separate list
        definition.constructors.put(callable.requiredArgs(), callable);
      }

      for(Method method : respClass.getMethods()) {
        JavaCallable callable = new JavaCallable(method);
        FunctionKey functionKey = new FunctionKey(callable.getName(), callable.requiredArgs()); 
        if (definition.functions.containsKey(functionKey)) {
          List<Callable> sameFuncKeys = definition.functions.get(functionKey);
          sameFuncKeys.add(callable);
        }
        else {
          definition.functions.put(functionKey, new ArrayList<Callable>(Arrays.asList(callable)));
        }
        //System.out.println("  ***PLACING METHOD: "+callable.getName()+" | "+callable.requiredArgs());
      }

      for(Field field : respClass.getFields()) {
        JavaVariableMember varMem = new JavaVariableMember(field, definition);
        definition.variables.put(field.getName(), varMem);
      }


      if (respClass.getSuperclass() != null) {
        definition.parent = defineClass(respClass.getSuperclass());
      }

      for(Class<?> inter : respClass.getInterfaces()) {
        definition.interfaces.add(defineClass(inter));
      }

      mappedClasses.put(respClass, definition);
      return definition;
    }
  }
}
