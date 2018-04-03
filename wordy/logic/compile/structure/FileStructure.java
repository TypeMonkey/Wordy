package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wordy.logic.common.FunctionKey;

public class FileStructure {
  
  private String fileName;
  
  private Map<String, ClassStruct> classes;
  private Map<FunctionKey, Function> functions; 
  private Map<String, Variable> variables;
  
  public FileStructure(String fileName) {
    this.fileName = fileName;
    
    this.functions = new HashMap<>();
    /*
     * To preserve the order of variable declaration
     */
    this.variables = new LinkedHashMap<>();
    this.classes = new HashMap<>();
  }
  
  public boolean addClass(ClassStruct struct) {
    if (classes.put(struct.getName().content(), struct) != null) {
      return true;
    }
    
    return false;
  }
  
  public boolean addFunction(Function function) {
    FunctionKey key = new FunctionKey(function.getName().content(), function.argAmount());
    if (functions.put(key, function) != null) {
      return true;
    }
    
    return false;
  }
  
  public boolean addVariable(Variable var) {
    if (variables.put(var.getName().content(), var) != null) {
      return true;
    }
    
    return false;
  }
  
  public List<ClassStruct> getClasses(){
    return new ArrayList<>(classes.values());
  }
  
  public List<Function> getFunctions(){
    return new ArrayList<>(functions.values());
  }
  
  public List<Variable> getVariables(){
    return new ArrayList<>(variables.values());
  }
  
  public ClassStruct getClass(String name) {
    return classes.get(name);
  }
  
  public Function getFunction(String name) {
    return functions.get(name);
  }
  
  public Variable getVariable(String name) {
    return variables.get(name);
  }

  public String getFileName() {
    return fileName;
  } 
}
