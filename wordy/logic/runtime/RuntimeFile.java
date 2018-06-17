package wordy.logic.runtime;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.ImportedFile;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.StackComponent;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.GenVisitor;
import wordy.logic.runtime.execution.JavaCallable;
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
public class RuntimeFile extends TypeDefinition{
    
  private FileInstance instance;
  private boolean initialized;
  
  /*
   * Imported java files.
   * 
   * We won't be loading the actual classes until we find a reference to their simple name, or alias
   * name while running the code. 
   * 
   * The key to this map will be the class' simple name, and value is it's fully qualified name
   */
  private Map<String, String> javaClasses; //imported java classes
  private Map<FunctionKey, List<Callable>> javaConstructors;
  private Map<String, TypeDefinition> typeDefs; //file classes
  
  public RuntimeFile(String name) {
    super(name);
    javaClasses = new HashMap<>();
    javaConstructors = new HashMap<>();
    typeDefs = new HashMap<>();
  }
  
  public FileInstance initialize(FileStructure structure, WordyRuntime runtime) {
    if (!initialized) {
      instance = new FileInstance(this);
      
      //Load imported classes first  
      //start with the standards (java.lang)
      for(String standard: WordyCompiler.JAVA_CLASSES) {
        String [] split = standard.split("\\.");
        javaClasses.put(split[split.length-1], standard);
        
        try {
          Class<?> curClass = Class.forName(standard);
          for(Constructor<?> constructor : curClass.getConstructors()) {
            FunctionKey consKey = new FunctionKey(curClass.getSimpleName(), constructor.getParameterCount());
            JavaCallable javaConstructor = new JavaCallable(constructor);
            if (javaConstructors.containsKey(consKey)) {
              javaConstructors.get(consKey).add(javaConstructor);
            }
            else {
              javaConstructors.put(consKey, new ArrayList<>(Arrays.asList(javaConstructor)));
            }
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Cannot load the class "+standard);
        }
      }
      
      //then the actual imports
      for(ImportedFile file: structure.getImports()) {
        String key = file.getTypeNameImported();
        if (file.getAlias() != null) {
          key = file.getAlias().content();
        }
        
        javaClasses.put(key, file.getImported());
        
        try {
          Class<?> curClass = Class.forName(file.getImported());
          for(Constructor<?> constructor : curClass.getConstructors()) {
            FunctionKey consKey = new FunctionKey(curClass.getSimpleName(), constructor.getParameterCount());
            if (file.getAlias() != null) {
              consKey = new FunctionKey(file.getAlias().content(), constructor.getParameterCount());
            }
            
            //System.out.println("--PLACED JVA CONSTRUCTOR: "+consKey.name+ " | "+consKey.argAmnt);
            JavaCallable javaConstructor = new JavaCallable(constructor);
            if (javaConstructors.containsKey(consKey)) {
              javaConstructors.get(consKey).add(javaConstructor);
            }
            else {
              javaConstructors.put(consKey, new ArrayList<>(Arrays.asList(javaConstructor)));
            }
          }
        } catch (ClassNotFoundException e) {
          throw new RuntimeException("Cannot load the class "+file.getImported());
        }
      }
      
      //now initialize the functions
      for(Function func: structure.getFunctions()) {
        FunctionMember functionMember = new FunctionMember(func.getName().content(), 
                                                           func.argAmount(), 
                                                           runtime, 
                                                           instance, 
                                                           func.getStatements());
        FunctionKey functionKey = new FunctionKey(functionMember.getName(), functionMember.requiredArgs());
        if (functions.containsKey(functionKey)) {
          functions.get(functionKey).add(functionMember);
        }
        functions.put(functionKey, Arrays.asList(functionMember));
      }
      
      //now add the classes, and add their constructors to the function map
      for(ClassStruct classStruct : structure.getClasses()) {
        TypeDefinition definition = TypeDefinition.constructDefinition(classStruct, runtime, instance);
        typeDefs.put(definition.getSimpleName(), definition);
        
        for(List<Callable> classFunctions : definition.getFunctions().values()) {
          for(Callable classFunc : classFunctions) {
            //places all constructor in the file's function context
            if (classFunc.isAConstructor()) {
              FunctionKey functionKey = new FunctionKey(classFunc.getName(), classFunc.requiredArgs());
              //System.out.println("***PLACING: "+functionKey.argAmnt+" | "+functionKey.name);
              if (functions.containsKey(functionKey)) {
                functions.get(functionKey).add(classFunc);
              }
              else {
                functions.put(functionKey, Arrays.asList(classFunc));
              }
            }
          }
        }
      }
      
      //finally, initialize and add file variables
      for(Variable fileVar : structure.getVariables()) {
        VariableMember member = new VariableMember(fileVar.getName().content(), 
                                                   fileVar.getExpression(), 
                                                   fileVar.isConstant());
        if (member.getExpr() != null) {
          Map [] varMaps = {variables};
          Map [] funcMaps = {functions, javaConstructors};
          RuntimeTable table = new RuntimeTable(varMaps, funcMaps, javaClasses);
          
          GenVisitor visitor = new GenVisitor(table, instance, runtime);
          member.getExpr().accept(visitor);
          
          StackComponent peeked = visitor.peekStack();
          if (peeked.isAnInstance()) {
            member.setValue((Instance) peeked);
          }
          else {
            VariableMember peekedVar = (VariableMember) peeked;
            member.setValue(peekedVar.getValue());
          }
        }
        variables.put(member.getName(), member);
      }
      
      initialized = true;
    }
    return instance;
  }
  
  public FileInstance getInstance() {
    return instance;
  }
  
  public Map<FunctionKey, List<Callable>> getJavaConstructors(){
    return javaConstructors;
  }
  
  public Map<String, TypeDefinition> getTypeDefs(){
    return typeDefs;
  } 
  
  public Map<String, String> getJavaClassMap(){
    return javaClasses;
  }
  
  public Map<String, VariableMember> getVariables(){
    return variables;
  }
  
  public String toString() {
    return "Source File: "+getName();
  }
}
