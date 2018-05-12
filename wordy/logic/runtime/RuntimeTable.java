package wordy.logic.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.GenVisitor;
import wordy.logic.runtime.types.ValType;

/**
 * Acts as a dictionary for identifiers
 * @author Jose Guaro
 *
 */
public class RuntimeTable {
  
  private static final Map<FunctionKey, EmbeddedFunction> embeddedFunctions;
  
  static {
    embeddedFunctions = loadEmbeddedFunctions();
  }

  private Map<String, VariableMember>[] varNameMaps;
  private Map<FunctionKey, FunctionMember>[] funcNameMaps;
  private Map<String, String> javaClassMap;


  /**
   * Constructs a RuntimeTable
   * @param vars - the array of Maps to use when looking for variables
   * @param funcs - the array of Maps to use when looking for functions
   */
  public RuntimeTable(Map<String,  VariableMember> [] vars, 
                      Map<FunctionKey, FunctionMember> [] funcs,
                      Map<String, String> javaClassMap) {
    varNameMaps = vars;
    funcNameMaps = funcs;
    this.javaClassMap = javaClassMap;
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
    for(int i = 0; i < varNameMaps.length; i++) {
      Map<String, VariableMember> current = varNameMaps[i];
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
  public Callable findCallable(FunctionKey key) {
    for(int i = 0; i < funcNameMaps.length; i++) {
      Map<FunctionKey, FunctionMember> current = funcNameMaps[i];
      if (current.containsKey(key)) {
        return current.get(key);
      }
    }
    
    if (embeddedFunctions.containsKey(key)) {
      return embeddedFunctions.get(key);
    }
    return null;
  }
  
  public String findBinaryName(String name) {
    return javaClassMap.get(name);
  }
  
  public boolean placeVariable(int arrIndex, VariableMember member) {
    return varNameMaps[arrIndex].put(member.getName(), member) != null;
  }
  
  public void clearVarMap(int arrIndex) {
    varNameMaps[arrIndex].clear();
  }
  
  public Callable findCallable(String name, int argc) {
    return findCallable(new FunctionKey(name, argc));
  } 
  
  public RuntimeTable clone() {
    Map<String, VariableMember>[] cloneVarMap = Arrays.copyOf(varNameMaps, varNameMaps.length);
    Map<FunctionKey, FunctionMember>[] cloneFuncNameMaps = Arrays.copyOf(funcNameMaps, funcNameMaps.length);
    
    return new RuntimeTable(cloneVarMap, cloneFuncNameMaps, new HashMap<>(javaClassMap));
  }
  
  private static Map<FunctionKey, EmbeddedFunction> loadEmbeddedFunctions(){
    HashMap<FunctionKey, EmbeddedFunction> map = new HashMap<>();
    
    EmbeddedFunction println = new EmbeddedFunction("println", 1, ValType.VOID) {
      public Constant call(GenVisitor visitor, RuntimeTable executor, Constant... args) {
        System.out.println(args[0].getValue());
        return Constant.VOID;
      }

    };
    map.put(new FunctionKey(println.getName(), println.requiredArgs()), println);
    
    EmbeddedFunction print = new EmbeddedFunction("print", 1, ValType.VOID) {
      public Constant call(GenVisitor visitor, RuntimeTable executor, Constant... args) {
        System.out.print(args[0].getValue());
        return Constant.VOID;
      }
    };    
    map.put(new FunctionKey(print.getName(), print.requiredArgs()), print);

    
    return map;
  }
  
}
