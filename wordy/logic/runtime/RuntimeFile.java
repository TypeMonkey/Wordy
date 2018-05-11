package wordy.logic.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.types.TypeDefinition;

/**
 * Represents a Wordy source file.
 * 
 * A RuntimeFile is invokable, as in all classes, functions and variables can be invoked
 * using this files name.
 * 
 * Ex: Say we have Sample.w . Sample.w has the class Capsule and the function hello()
 *  
 *  We can invoke Capsule's constructor doing
 *    Sample.Capsule()
 *  We can also invoke hello()  doing
 *    Sample.hello()
 * 
 * @author Jose Guaro
 *
 */
public class RuntimeFile {
  
  private String name;
  
  private Map<String, VariableMember> fileVars; //file variables
  private Map<String, TypeDefinition> typeDefs; //file classes
  private Map<FunctionKey, Callable> functions; //file functions
  
  public RuntimeFile(String name) {
    this.name = name;
    fileVars = new LinkedHashMap<>();
    typeDefs = new LinkedHashMap<>();
    functions = new LinkedHashMap<>();
  }
  
  public String getName() {
    return name;
  }
}
