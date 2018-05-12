package wordy.logic.runtime.types;

import java.util.ArrayList;
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
import wordy.logic.runtime.execution.Constructor;
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
  protected Map<FunctionKey, FunctionMember> functions;
  protected ValType type;
  protected String name;
  
  protected TypeDefinition(String name, 
                           Map<String, VariableMember> variables, 
                           Map<FunctionKey, FunctionMember> functions) {
    this.name = name;
    this.variables = variables;
    this.functions = functions;
    this.type = new ValType(name);
  }
  
  protected TypeDefinition(String name) {
    variables = new LinkedHashMap<>();
    functions = new HashMap<>();
    this.name = name;
    this.type = new ValType(name);
  }
  
  public String getName() {
    return name;
  }
  
  public FunctionMember findFunction(String name, int argc) {
    return findFunction(new FunctionKey(name, argc));
  }
  
  public FunctionMember findFunction(FunctionKey key) {
    return functions.get(key);
  }
  
  public VariableMember findVariable(String name) {
    return variables.get(name);
  }
  
  public Map<FunctionKey, FunctionMember> getFunctions() {
    return new HashMap<>(functions);
  }
  
  public Map<String, VariableMember> getVariables(){
    return new LinkedHashMap<>(variables);
  }
  
  public ValType getType() {
    return type;
  }
  
  /**
   * Creates a TypeDefinition instance based off a ClassStruct.
   * @param struct - the ClassStruct to base off this TypeDefinition
   * @return the TypeDefinition based of struct
   */
  public static TypeDefinition constructDefinition(ClassStruct struct, WordyRuntime runtime) {
    TypeDefinition definition = new TypeDefinition(struct.getName().content());
    
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
      System.out.println("BUILDING TYPE: "+struct.getName().content()+" | "+function);
      if (function.isConstructor()) {
        functionMember = new Constructor(funcName, argc, function.getStatements(), definition, runtime);
        constructorFound = true;
      }
      else {
        functionMember = new FunctionMember(function.getName().content(), 
                                            argc, 
                                            runtime,
                                            function.getStatements());
      }
      definition.functions.put(new FunctionKey(funcName, argc), functionMember);
    }
    
    /*
     * No constructor was found. So add default constructror 
     */
    if (!constructorFound) {
      Constructor defaultCons = new Constructor(struct.getName().content(), 0, null, definition, runtime);
      definition.functions.put(new FunctionKey(defaultCons.getName(), defaultCons.requiredArgs()), defaultCons);
    }
    
    return definition;
  }
  
}
