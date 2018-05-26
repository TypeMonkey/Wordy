package wordy.logic.runtime.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.ConstructorFunction;
import wordy.logic.runtime.execution.FunctionMember;

/**
 * Represents a custom type definition - notable a class or
 * blueprint for a custom object.
 * 
 * Note: Instance variables are only initialized when an object
 * if created based off a TypeDefinition. i.e, instance variables
 * are initialized once a constructor is invoked
 * 
 * @author Jose Guaro
 *
 */
public class TypeDefinition{
    
  protected Map<String, VariableMember> variables;
  protected Map<FunctionKey, List<Callable>> functions;
  protected TypeDefinition parent;
  
  protected List<JavaClassDefinition> interfaces;
  
  protected String name;
  
  protected TypeDefinition(String name,
                           TypeDefinition parent,
                           Map<String, VariableMember> variables, 
                           Map<FunctionKey, List<Callable>> functions) {
    this.name = name;
    this.variables = variables;
    this.functions = functions;
    this.parent = parent;
    interfaces = new ArrayList<>();
  }
  
  protected TypeDefinition(String name) {
    this(name, null, new LinkedHashMap<>(), new HashMap<>());
  }
  
  public String getName() {
    return name;
  }
  
  public boolean equals(Object object) {
    if (object instanceof TypeDefinition) {
      TypeDefinition definition = (TypeDefinition) object;
      return name.equals(definition.getName());
    }
    return false;
  }
  
  public String toString() {
    return name;
  }
  
  public String getSimpleName() {
    String [] splitted = name.split("\\.");
    return splitted[splitted.length-1];
  }
  
  public List<Callable> findFunction(String name, int argc) {
    return findFunction(new FunctionKey(name, argc));
  }
  
  public List<Callable> findFunction(FunctionKey key) {
    return functions.get(key);
  }
  
  public VariableMember findVariable(String name) {
    return variables.get(name);
  }
  
  public Map<FunctionKey, List<Callable>> getFunctions() {
    return new HashMap<>(functions);
  }
  
  public Map<String, VariableMember> getVariables(){
    return new LinkedHashMap<>(variables);
  }
  
  public TypeDefinition getParent() {
    return parent;
  }
  
  public List<JavaClassDefinition> getInterfaces(){
    return interfaces;
  }
    
  /**
   * Checks if the given TypeDefinition is a child of this TypeDefinition
   * @param definition
   * @return
   */
  public boolean isChildOf(TypeDefinition definition) {
    if (parent.equals(definition) || parent.isChildOf(definition)) {
      return true;
    }
    else {
      for(TypeDefinition par: interfaces) {
        if (par.isChildOf(definition)) {
          return true;
        }
      }
      return false;
    }
  }
  
  /**
   * Creates a TypeDefinition instance based off a ClassStruct.
   * @param struct - the ClassStruct to base off this TypeDefinition
   * @return the TypeDefinition based of struct
   */
  public static TypeDefinition constructDefinition(ClassStruct struct, 
                                                   WordyRuntime runtime, 
                                                   FileInstance currentFile) {
    TypeDefinition definition = runtime.findTypeDef(currentFile.getName(), struct.getName().content());
    if (definition != null) {
      return definition;
    }
    
    definition = new TypeDefinition(struct.getFullName());
    
    for(Variable member: struct.getVariables()) {
      VariableMember mem = new VariableMember(member.getName().content(), 
                                              member.getExpression(), 
                                              member.isConstant());
      definition.variables.put(member.getName().content(),mem);
    }
    
    boolean constructorFound = false;
    
    for(Function function: struct.getFunctions()) {
      String funcName = function.getName().content();
      int argc = function.argAmount();
      FunctionMember functionMember = null;
      //System.out.println("BUILDING TYPE: "+struct.getName().content()+" | "+function);
      if (function.isConstructor()) {
        functionMember = new ConstructorFunction(funcName, 
                                                 argc, 
                                                 function.getStatements(), 
                                                 definition, 
                                                 currentFile, 
                                                 runtime);
        constructorFound = true;
      }
      else {
        functionMember = new FunctionMember(function.getName().content(), 
                                            argc, 
                                            runtime,
                                            currentFile,
                                            function.getStatements());
      }
      definition.functions.put(new FunctionKey(funcName, argc), Arrays.asList(functionMember));
    }
    
    /*
     * No constructor was found. So add default constructror 
     */
    if (!constructorFound) {
      ConstructorFunction defaultCons = new ConstructorFunction(struct.getName().content(), 
                                                                0, 
                                                                null, 
                                                                definition, 
                                                                currentFile, 
                                                                runtime);
      definition.functions.put(new FunctionKey(defaultCons.getName(), defaultCons.requiredArgs()), 
                               Arrays.asList(defaultCons));
    }
    
    return definition;
  }
  
  /**
   * Checks if the given TypeDefinition (Wordy class) coherently
   * follows its parent. Ex: implements abstract methods, calls parent constructor, etc.
   */
  public static void enforceInheritance(TypeDefinition definition, WordyRuntime runtime) {
    
  }
  
  /**
   * 
   */
  public static class RawTypeInfo{
    
  }
}
