package wordy.logic.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.EmbeddedFunction;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.GenVisitor;

/**
 * Acts as a dictionary for identifiers - be it functions and variables.
 * 
 * The table keeps two lists of maps - the first holds mappings to variables, and the latter to functions.
 *  The list for maps on variables contains at least one map, and the very first map
 *  is reserved for local variables (be it local to the function or current block). 
 *  The other maps can be used for non-local variables.
 * 
 * @author Jose Guaro
 *
 */
public class RuntimeTable {
  
  private static Map<FunctionKey, EmbeddedFunction> embeddedFunctions;

  private List<Map<String, VariableMember>> varNameMaps;
  private List<Map<FunctionKey, List<Callable>>> funcNameMaps;
  private Map<String, String> javaClassMap;


  /**
   * Constructs a RuntimeTable
   * @param vars - the array of Maps to use when looking for variables
   * @param funcs - the array of Maps to use when looking for functions
   */
  public RuntimeTable(Map<String,  VariableMember> [] vars, 
                      Map<FunctionKey, List<Callable>> [] funcs,
                      Map<String, String> javaClassMap) {
    varNameMaps = new ArrayList<>();
    varNameMaps.addAll(Arrays.asList(vars));
    
    funcNameMaps = new ArrayList<>(Arrays.asList(funcs));
    this.javaClassMap = javaClassMap;
    
    if (embeddedFunctions == null) {
      embeddedFunctions = loadEmbeddedFunctions();
    }
  }

  /**
   * Finds a variable in this table's variable maps
   * 
   * The function first queries the first map in the variable map array provided
   * when the constructor was invoked. It then sequentially queries the following maps
   * in the array until it finds a VariableMember
   * 
   * @param name - the name of the variable
   * @return the corresponding VariableMember, or null if no VariableMember was found
   */
  public VariableMember findVariable(String name) {
    for(Map<String, VariableMember> current : varNameMaps) {
      //System.out.println(current == null);
      if (current.containsKey(name)) {
        return current.get(name);
      }
    }
    
    return null;
  }
  
  /**
   * Finds a function in this table's function maps
   * 
   * The function first queries the first map in the function map array provided
   * when the constructor was invoked. It then sequentially queries the following maps
   * in the array until it finds a FunctionMember
   * 
   * @param key - the FunctionKey of the function to look for
   * @return the corresponding FunctionMember, or null if none was found
   */
  public List<Callable> findCallable(FunctionKey key) {
    for(Map<FunctionKey, List<Callable>> current : funcNameMaps) {
      if (current.containsKey(key)) {
        return current.get(key);
      }
    }
    
    if (embeddedFunctions.containsKey(key)) {
      return Arrays.asList(embeddedFunctions.get(key));
    }
    return null;
  }
  
  public List<Callable> findCallable(String name, int argc) {
    return findCallable(new FunctionKey(name, argc));
  } 
  
  public String findBinaryName(String name) {
    return javaClassMap.get(name);
  }
  
  /**
   * Places the given variable in the local variable map 
   * (The first variable map in this table)
   * @param member - the VariableMember to add
   * @return true - if the variable's name has already been mapped, false if else
   */
  public boolean placeLocalVar(VariableMember member) {
    return varNameMaps.get(0).put(member.getName(), member) != null;
  }
  
  /**
   * Clears the local variable map.
   */
  public void clearLocalVars() {
    varNameMaps.get(0).clear();
  }
  
  public void addVariableMap(Map<String, VariableMember> varMap) {
    varNameMaps.add(varMap);
  }
  
  public void addFuncMap(Map<FunctionKey, List<Callable>> funcMap) {
    funcNameMaps.add(funcMap);
  }
  
  @SuppressWarnings("unchecked")
  public RuntimeTable clone(boolean keepLocalVar) {
    //System.out.println("----ABOUT TO CLONE");
    Map<String, VariableMember>[] cloneVarMap = new Map[varNameMaps.size()];
    
    if (keepLocalVar) {
      cloneVarMap[0] = new HashMap<>(varNameMaps.get(0));
    }
    else {
      cloneVarMap[0] = new HashMap<>();
    }
    
    for(int i = 1; i < cloneVarMap.length; i++) {
      //System.out.println("CLONING: "+(varNameMaps.get(i) == null));
      cloneVarMap[i] = new HashMap<>(varNameMaps.get(i));
    }
    
    Map<FunctionKey, List<Callable>>[] cloneFuncNameMaps = new Map[funcNameMaps.size() ];    
    for(int i = 0; i < cloneFuncNameMaps.length; i++) {
      //System.out.println("CLONING FUNCS: "+(funcNameMaps.get(i) == null));
      cloneFuncNameMaps[i] = new HashMap<>(funcNameMaps.get(i));
    }
    
    //System.out.println("----DONE CLONE "+cloneVarMap.length);
    return new RuntimeTable(cloneVarMap, cloneFuncNameMaps, new HashMap<>(javaClassMap));
  }
  
  private static Map<FunctionKey, EmbeddedFunction> loadEmbeddedFunctions(){
    HashMap<FunctionKey, EmbeddedFunction> map = new HashMap<>();
    
    EmbeddedFunction println = new EmbeddedFunction("println", 1, null) {
      @Override
      public Instance call(GenVisitor visitor, RuntimeTable table, Instance... args) {
        System.out.println(args[0]);
        return null;
      }

    };
    map.put(new FunctionKey(println.getName(), println.requiredArgs()), println);
    
    EmbeddedFunction print = new EmbeddedFunction("print", 1, null) {
      @Override
      public Instance call(GenVisitor visitor, RuntimeTable table, Instance... args) {
        System.out.print(args[0]);
        return null;
      }

    };    
    map.put(new FunctionKey(print.getName(), print.requiredArgs()), print);

    
    return map;
  }
  
}
