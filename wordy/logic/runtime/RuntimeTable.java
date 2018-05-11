package wordy.logic.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.GenVisitor;
import wordy.logic.runtime.types.TypeInstance;

/**
 * Holds runtime information for program execution, such
 * as variable and function names and definition
 * @author Jose Guaro
 *
 */
public class RuntimeTable {

  private Map<String, VariableMember> instanceVars;
  private Map<String, VariableMember> localVars;
  private Map<String, VariableMember> fileVars;
  private Map<FunctionKey, Callable> callables;
  private Map<FunctionKey, SystemFunction> systemFunctions;

  public RuntimeTable() {
    this.instanceVars = new HashMap<>();
    this.localVars = new HashMap<>();
    this.fileVars = new HashMap<>();
    this.callables = new HashMap<>();
    this.systemFunctions = new HashMap<>();
  }

  public void initialize(List<VariableMember> instanceVars,
                         List<VariableMember> lclVars, 
                         List<VariableMember> filVar, 
                         List<Callable> fileFus, 
                         List<SystemFunction> sysFus) {
    
   
    
    if (instanceVars != null) {
      for(VariableMember instance: instanceVars) {
        this.instanceVars.put(instance.getName(), instance);
      }
    }
    
    if (lclVars != null) {
      for(VariableMember member: lclVars) {
        localVars.put(member.getName(), member);
      }

    }

    if (filVar != null) {
      for(VariableMember member: filVar) {
        fileVars.put(member.getName(), member);
      }
    }

    if (fileFus != null) {
      for(Callable callable: fileFus) {
        callables.put(new FunctionKey(callable.getName(), callable.requiredArgs()), callable);
      }

    }

    if (sysFus != null) {
      for(SystemFunction sysFun: sysFus) {
        System.out.println("---SYS FUNC: "+sysFun.getName()+" | "+sysFun.requiredArgs());
        systemFunctions.put(new FunctionKey(sysFun.getName(), sysFun.requiredArgs()), sysFun);
      }
    }
  }

  public RuntimeTable clone() {
    RuntimeTable executor = new RuntimeTable();
    executor.callables = new HashMap<>(callables);
    executor.fileVars = new HashMap<>(fileVars);
    executor.localVars = new HashMap<>(localVars);
    executor.systemFunctions = new HashMap<>(systemFunctions);
    return executor; 
  }
  
  /**
   * Executes the program
   * @param function - the function's name at which to start execution at
   */
  public void execute(String function, int argc,  Constant ... constants) {
    Callable starter = findCallable(function, argc);
    if (starter == null) {
      throw new RuntimeException("Couldn't find the function '"+function+"'");
    }
    else {     
      starter.call(new GenVisitor(this), this, constants);
    }
  }

  /**
   * Places the given VariableMember in this executor's local
   * variable map
   * @param variableMember - the VariableMember to add
   * @return true - if this VariableMember's name has already been mapped
   *                to an existing variable.
   *         false - if else.
   */
  public boolean placeLocalVar(VariableMember variableMember) {
    if (localVars.containsKey(variableMember.getName())) {
      return true;
    }
    else {
      localVars.put(variableMember.getName(), variableMember);
      return false;
    }
  }
  
  /**
   * Places the given VariableMember in this executor's file variable map
   * @param variableMember - the VariableMember to add
   * @return true - if this VariableMember's name has already been mapped
   *                to an existing variable.
   *         false - if else.
   */
  public boolean placeFileVar(VariableMember variableMember) {
    if (fileVars.containsKey(variableMember.getName())) {
      return true;
    }
    else {
      fileVars.put(variableMember.getName(), variableMember);
      return false;
    } 
  }
  
  public Callable findCallable(FunctionKey key) {
    return callables.get(key);
  }
  
  public SystemFunction findSystemFunction(FunctionKey key) {
    return systemFunctions.get(key);
  }

  public Callable findCallable(String name, int argc) {
    return findCallable(new FunctionKey(name, argc));
  }
  
  public SystemFunction findSystemFunction(String name, int argc) {
    return findSystemFunction(new FunctionKey(name, argc));
  }
  
  public VariableMember findFileVariable(String name) {
    return fileVars.get(name);
  }

  public VariableMember findLocalVariable(String name) {
    return localVars.get(name);
  }
  
  public VariableMember findInstanceVariable(String name) {
    return instanceVars.get(name);
  }

  public List<VariableMember> getLocalVars() {
    return new ArrayList<>(localVars.values());
  }

  public List<VariableMember> getInstanceVars() {
    return new ArrayList<>(instanceVars.values());
  }
  
  public List<VariableMember> getFileVars() {
    return new ArrayList<>(fileVars.values());
  }

  public List<Callable> getCallables() {
    return new ArrayList<>(callables.values());
  }

  public List<SystemFunction> getSystemFunctions() {
    return new ArrayList<>(systemFunctions.values());
  }
}
