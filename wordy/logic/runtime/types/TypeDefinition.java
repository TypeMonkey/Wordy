package wordy.logic.runtime.types;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.Token;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.VariableMember;
import wordy.logic.runtime.WordyRuntime;
import wordy.logic.runtime.components.FileInstance;
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
  protected Map<Integer, FunctionMember> constructors; //constructors mapped by the amount of arguments they accept
  protected TypeDefinition parent;
    
  protected String name;
  
  private ClassStruct struct;
  
  protected TypeDefinition(String name,
                           TypeDefinition parent,
                           Map<String, VariableMember> variables, 
                           Map<FunctionKey, List<Callable>> functions,
                           Map<Integer, FunctionMember> constructors) {
    this.name = name;
    this.variables = variables;
    this.functions = functions;
    this.parent = parent;
    this.constructors = constructors;
  }
  
  protected TypeDefinition(String name) {
    this(name, null, new LinkedHashMap<>(), new HashMap<>(), new HashMap<>());
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
  
  public FunctionMember findConstructor(int argc) {
    return constructors.get(argc);
  }
  
  public List<Callable> findFunction(String name, int argc) {
    return findFunction(new FunctionKey(name, argc));
  }
  
  public List<Callable> findFunction(FunctionKey key) {
    if (functions.containsKey(key)) {
      return functions.get(key);
    }
    
    //System.out.println("IS NULL? "+(parent == null)+" "+(getAttachedStruct() == null)+" | "+getClass().getName()+" | "+key+" | "+name);
    if (parent == null) {
      /**
       * In case function searches reach all the way to java.lang.Object. 
       */
      return null;
    }
    return parent.findFunction(key);
  }
  
  public VariableMember findVariable(String name) {
    if (variables.containsKey(name)) {
      return variables.get(name);
    }
    
    if (parent == null) {
      /**
       * In case function searches reach all the way to java.lang.Object. 
       */
      return null;
    }
    return parent.findVariable(name);
  }
  
  public Map<FunctionKey, List<Callable>> getFunctions() {
    return new HashMap<>(functions);
  }
  
  public Map<String, VariableMember> getVariables(){
    return new LinkedHashMap<>(variables);
  }
  
  public Map<Integer, FunctionMember> getConstructors(){
    return constructors;
  }
  
  public TypeDefinition getParent() {
    return parent;
  }
  
  public boolean isAnInterface() {
    return false;
  }
  
  private void attchClassStruct(ClassStruct struct) {
    this.struct = struct;
  }
  
  private ClassStruct getAttachedStruct() {
    return struct;
  }
    
  /**
   * Checks if the given TypeDefinition is a superclass to this TypeDefinition
   * @param definition - the TypeDefinition to check
   * @return 
   */
  public boolean isChildOf(TypeDefinition definition) {
    return (equals(definition) || parent.equals(definition) || parent.isChildOf(definition));
  }
  
  
  /**
   * Creates a TypeDefinition instance based off a ClassStruct.
   * @param struct - the ClassStruct to base off this TypeDefinition
   * @return the TypeDefinition based of struct
   */
  public static TypeDefinition constructDefinition(ClassStruct struct, 
                                                   WordyRuntime runtime, 
                                                   FileInstance currentFile) {
    TypeDefinition definition = new TypeDefinition(struct.getFullName());
    definition.attchClassStruct(struct);
    
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
        definition.constructors.put(functionMember.requiredArgs(), functionMember);
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
                                                                new Statement[0], 
                                                                definition, 
                                                                currentFile, 
                                                                runtime);
      FunctionKey functionKey = new FunctionKey(defaultCons.getName(), defaultCons.requiredArgs());
      definition.functions.put(functionKey, Arrays.asList(defaultCons));
      definition.constructors.put(defaultCons.requiredArgs(), defaultCons);
      //System.out.println("--------->>>ADDED DEAFULE CONSTRUCTOR FOR "+definition.name+" | "+defaultCons.requiredArgs());
    }
    
    return definition;
  }
  
  public static void includeInhertianceInfo(WordyRuntime runtime, TypeDefinition definition, FileInstance current) {
    //System.out.println("------!FOR CLASS: "+definition.getName()+"!--------");
    ClassStruct originalStruct = definition.getAttachedStruct();
    
    TypeDefinition parent = null;
    if (originalStruct.getParentClass() == null || originalStruct.getParentClass().length == 0) {
      parent = JavaClassDefinition.defineClass(Object.class);
    }
    else {
      Token [] parentFull = originalStruct.getParentClass();
      boolean foundParent = false;
      if (parentFull.length == 1) {
        //non-binary class name. First check file-local classes.
        //If not found then, check the imports of this file
        parent = current.getDefinition().getTypeDefs().get(parentFull[0].content());
        if (parent == null) {
          String fullJavaName = current.getDefinition().getJavaClassMap().get(parentFull[0].content());
          if (fullJavaName == null) {
            throw new RuntimeException("Can't find the parent class '"+parentFull[0].content()+"'");
          }
          else {
            try {
              parent = JavaClassDefinition.defineClass(Class.forName(fullJavaName));
              foundParent = true;
            } catch (ClassNotFoundException e) {
              throw new RuntimeException("Can't find the parent class '"+parentFull[0].content()+"'");
            }
          }
        }
        else {
          foundParent = true;
        }
      }
      else if (parentFull.length == 3) {
        FileInstance fileInstance = runtime.findFile(parentFull[0].content());
        if (fileInstance == null) {
          foundParent = false;
        }
        else {
          parent = fileInstance.getDefinition().getTypeDefs().get(parentFull[2].content());
          if (parent == null) {
            foundParent = false;
            //System.out.println(fileInstance.getDefinition().getTypeDefs().keySet());
            //System.out.println("*****NOT FOUND: "+parentFull[2].content());
          }
          else {
            foundParent = true;
          }
        }
      }
      
      
      //assume this parent is a Java class (the full name is their binary name). If so, find it.
      if (foundParent == false) {
        String fullParentName = "";
        for(Token t:parentFull) {
          fullParentName += t.content();
        }
        
        try {
          parent = JavaClassDefinition.defineClass(Class.forName(fullParentName));
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Can't find the parent class '"+fullParentName+"'");
        }
      }
    }
    
    //set found parent to definition's parent
    //System.out.println("----PARENT FOUND? "+parent+" | "+Arrays.toString(originalStruct.getParentClass()));
    definition.parent = parent;
  }
  
}
