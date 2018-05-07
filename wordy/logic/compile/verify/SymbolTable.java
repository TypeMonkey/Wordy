package wordy.logic.compile.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Variable;

public class SymbolTable {
  
  private Map<String, ClassStruct> classes;
  private Map<String, Variable> variables;
  private Map<FunctionKey, Function> functions;
  private List<FunctionKey> systemFuncs;
  private Map<String, Class<?>> systemClasses;
  
  public SymbolTable() {
    classes = new HashMap<>();
    variables = new HashMap<>();
    functions = new HashMap<>();
    systemFuncs = new ArrayList<>();
    systemClasses = new HashMap<>();
  }
  
  public SymbolTable clone() {
    SymbolTable table = new SymbolTable();
    table.classes = new HashMap<>(classes);
    table.variables = new HashMap<>(variables);
    table.functions = new HashMap<>(functions);
    table.systemFuncs = new ArrayList<>(systemFuncs);
    table.systemClasses = new HashMap<>(systemClasses);
    return table;
  }
  
  /**
   * Places a variable in this symbol table
   * @param variable - the Variable to place
   * @return true if this Variable's name has been mapped already
   *         false if it hasn't
   * 
   */
  public boolean placeVariable(Variable variable) {
    if (variables.put(variable.getName().content(), variable) != null) {
      return true;
    }
    return false;
  }
  
  /**
   * Places a function in this symbol table
   * @param variable - the Variable to place
   * @return true if this Variable's name has been mapped already
   *         false if it hasn't
   * 
   */
  public boolean placeFunction(Function function) {
    FunctionKey key = new FunctionKey(function.getName().content(), function.argAmount());
    if (functions.put(key, function) != null) {
      return true;
    }
    return false;
  }
  
  /**
   * Places a Class in this symbol table
   * @param struct - the Class to place
   * @return true if this Class' name has been mapped already
   *         false if it hasn't
   * 
   */
  public boolean placeClass(ClassStruct struct) {
    if (classes.put(struct.getName().content(), struct) != null) {
      return true;
    }
    return false;
  }
  
  /**
   * Places the FunctionKey - representing a system function - in this SymbolTable
   * @param key - the key to add
   * @return true if this key has been added already
   *         false if it hasn't
   */
  public boolean placeSystemFunction(FunctionKey key) {
    if (systemFuncs.contains(key)) {
      return true;
    }
    systemFuncs.add(key);
    return false;
  }
  
  /**
   * Places a System Class in this SymbolTable
   * @param key - the key to add
   * @return true if this key has been added already
   *         false if it hasn't
   */
  public boolean placeSystemFunction(String className, Class<?> sysClass) {
    if (systemClasses.put(className, sysClass) != null) {
      return true;
    }
    return false;
  }
  
  
  public ClassStruct getClass(String name) {
    return classes.get(name);
  }
  
  public Function getFunction(String name, int argCnt) {
    return functions.get(new FunctionKey(name, argCnt));
  }
  
  public boolean systemFunctionExists(String name, int argcCnt) {
    return systemFuncs.contains(new FunctionKey(name, argcCnt));
  }
  
  public Class<?> getSystemClass(String name){
    return systemClasses.get(name);
  }
  
  public Variable getVariable(String name) {
    return variables.get(name);
  }

  public List<FunctionKey> getSystemFunctions(){
    return systemFuncs;
  }
}
