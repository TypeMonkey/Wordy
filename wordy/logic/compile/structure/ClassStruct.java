package wordy.logic.compile.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.ReservedSymbols;
import wordy.logic.compile.Token;

public class ClassStruct {
  
  private String fileName;
  private Token name;
  private Map<FunctionKey, Function> functions; 
  private Map<String, Variable> variables;
  
  private Token parentClass;
  private List<Token> interfaces; //the interfaces implemented by this class
  
  public ClassStruct(String fileName, Token name) {
    this(fileName, name, null, new ArrayList<>());
  }
  
  public ClassStruct(String fileName, Token name, Token parent, List<Token> interfaces) {
    this.fileName = fileName;
    this.name = name;
    this.parentClass = parent;
    this.functions = new HashMap<>();
    this.variables = new LinkedHashMap<>();
    this.interfaces = interfaces;
  }
  
  public boolean equals(Object object) {
    if (object instanceof ClassStruct) {
      ClassStruct struct = (ClassStruct) object;
      return struct.name.content().equals(this.name.content());    
    }
    return false;
  }
  
  /**
   * Adds the given variable to this class' variable map
   * @param variable - the Variable to add
   * @return true if this variable HASN'T been mapped yet
   *         false if this variable HAS been mapped
   */
  public boolean addVariable(Variable variable) {
    return variables.put(variable.getName().content(), variable) == null;
  }
  
  /**
   * Adds the given function to this class' function map
   * @param function - the Function to add
   * @return true if this function HASN'T been mapped yet
   *         false if this function HAS been mapped
   */
  public boolean addFunction(Function function) {
    FunctionKey key = new FunctionKey(function.getName().content(), function.argAmount());
    return functions.put(key, function) == null;
  }
  
  public void setParent(Token parentClass) {
    this.parentClass = parentClass;
  }
  
  public void addInterface(Token interfaceName) {
    interfaces.add(interfaceName);
  }
  
  public String getFullName() {
    return fileName+ReservedSymbols.DOT+name.content();
  }
  
  public Token getName() {
    return name;
  }
  
  public List<Token> getInterfaces(){
    return interfaces;
  }
  
  public Token getParentClass() {
    return parentClass;
  }
  
  public Function getFunction(String name, int argCount) {
    return functions.get(new FunctionKey(name, argCount));
  }
  
  public Variable getVariable(String name) {
    return variables.get(name);
  }
  
  public List<Variable> getVariables(){
    return new ArrayList<>(variables.values());
  }
  
  public List<Function> getFunctions() {
    return new ArrayList<>(functions.values());
  }
}
