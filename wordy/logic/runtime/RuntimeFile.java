package wordy.logic.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import wordy.logic.common.FunctionKey;
import wordy.logic.compile.WordyCompiler;
import wordy.logic.compile.structure.ClassStruct;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Function;
import wordy.logic.compile.structure.ImportedFile;
import wordy.logic.compile.structure.Variable;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.types.TypeDefinition;
import wordy.logic.runtime.types.ValType;

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
  private String name;
  
  /*
   * Imported java files.
   * 
   * We won't be loading the actual classes until we find a reference to their simple name, or alias
   * name while running the code. 
   * 
   * The key to this map will be the class' simple name, and value is it's fully qualified name
   */
  private Map<String, String> javaClasses; //imported java classes
  private Map<String, TypeDefinition> typeDefs; //file classes
  
  public RuntimeFile(String name) {
    super(name);
    this.name = name;
    javaClasses = new HashMap<>();
    typeDefs = new HashMap<>();
  }
  
  public FileInstance initialize(FileStructure structure, WordyRuntime runtime) {
    if (!initialized) {
      
      //Load imported classes first  
      //start with the standards (java.lang)
      for(String standard: WordyCompiler.JAVA_CLASSES) {
        String [] split = standard.split("\\.");
        javaClasses.put(split[split.length-1], standard);
      }
      
      //then the actual imports
      for(ImportedFile file: structure.getImports()) {
        String key = file.getTypeNameImported();
        if (file.getAlias() != null) {
          key = file.getAlias().content();
        }
        
        javaClasses.put(key, file.getImported());
      }
      
      //now initialize the functions
      for(Function func: structure.getFunctions()) {
        FunctionMember functionMember = new FunctionMember(func.getName().content(), 
                                                           func.argAmount(), 
                                                           runtime, func.getStatements());
        functions.put(new FunctionKey(functionMember.getName(), functionMember.requiredArgs()), functionMember);
      }
      
      //now add the classes, and add their constructors to the function map
      for(ClassStruct classStruct : structure.getClasses()) {
        TypeDefinition definition = TypeDefinition.constructDefinition(classStruct, runtime);
        typeDefs.put(definition.getName(), definition);
        
        for(FunctionMember classFunc : definition.getFunctions().values()) {
          if (classFunc.isAConstructor()) {
            functions.put(new FunctionKey(classFunc.getName(), classFunc.requiredArgs()), classFunc);
          }
        }
      }
      
      //finally, initialize and add file variables
      for(Variable fileVar : structure.getVariables()) {
        VariableMember member = new VariableMember(fileVar.getName().content(), 
                                                   fileVar.getExpression(), 
                                                   fileVar.isConstant());
        
      }
      
      initialized = true;
      instance = new FileInstance(this);
    }
    return instance;
  }
  
  public FileInstance getInstance() {
    return instance;
  }
  
  public Map<String, TypeDefinition> getTypeDefs(){
    return typeDefs;
  } 
  
  public String toString() {
    return "Source File: "+getName();
  }
}
