package wordy.logic.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wordy.logic.compile.nodes.ASTNode.NodeType;
import wordy.logic.compile.nodes.MethodCallNode;
import wordy.logic.compile.structure.FileStructure;
import wordy.logic.compile.structure.Statement;
import wordy.logic.compile.structure.Statement.StatementDescription;
import wordy.logic.runtime.components.FileInstance;
import wordy.logic.runtime.components.Instance;
import wordy.logic.runtime.components.JavaInstance;
import wordy.logic.runtime.errors.FatalInternalException;
import wordy.logic.runtime.errors.InvocationException;
import wordy.logic.runtime.execution.Callable;
import wordy.logic.runtime.execution.FunctionMember;
import wordy.logic.runtime.execution.GenVisitor;
import wordy.logic.runtime.types.TypeDefinition;

/**
 * Front-end for executing a Wordy program
 * @author Jose Guaro
 *
 */
public class WordyRuntime {
  
  private Map<String, FileInstance> files;
  private boolean runtimeInitialized;
  
  public WordyRuntime() {
    files = new HashMap<>();
  }
  
  public void initialize(Map<String, FileStructure> sources) {
    if (!runtimeInitialized) {
      for(FileStructure structure : sources.values()) {
        RuntimeFile file = new RuntimeFile(structure.getFileName());
        FileInstance instance = file.initialize(structure, this);
        
        files.put(file.getName(), instance);
      }
      
      enforceInheritance();
      runtimeInitialized = true;
    }
  }
  
  private void enforceInheritance() {
    ArrayList<TypeDefinition> allDefs = new ArrayList<>();
    for(FileInstance instance : files.values()) {
      RuntimeFile file = instance.getDefinition();
      for(TypeDefinition definition : file.getTypeDefs().values()) {
        TypeDefinition.includeInhertianceInfo(this, definition, instance);
        
        //Now, check the super class' constructors.
        //If all constructors of the parent require at least one argument, then check 
        //the child's constructors for a super() 
        
        //System.out.println("----DEF: "+definition.getName()+" | "+definition.getParent());
        boolean mustCheck = false;
        Map<Integer, FunctionMember> superConstructors = definition.getParent().getConstructors();
        for(FunctionMember supCons : superConstructors.values()) {
          if (supCons.requiredArgs() > 0) {
            mustCheck = true;
          }
        }
        
        if (mustCheck) {
          for(FunctionMember constructor : definition.getConstructors().values()) {
            Statement [] constStates = constructor.getStatements();
            if (constStates == null || (constStates.length - constructor.requiredArgs()) < 1) {
              throw new RuntimeException("The constructor for "+definition.getName()+" that takes "+constructor.requiredArgs()+
                                         " arguments must first invoke a constructor to its parent, "+definition.getParent().getName());
            }
            else {
              Statement firstStatement = constStates[constructor.requiredArgs()];
              //System.out.println("----first? "+firstStatement.getDescription()+" | "+definition.getName()+" | "+constStates.length);
              if (firstStatement.getDescription() !=  StatementDescription.REGULAR || 
                  firstStatement.getExpression().nodeType() != NodeType.FUNC_CALL) {
                throw new RuntimeException("The constructor for "+definition.getName()+" that takes "+constructor.requiredArgs()+
                    " arguments must first invoke a constructor to its parent, "+definition.getParent().getName());
              }
              else {
                MethodCallNode callNode = (MethodCallNode) firstStatement.getExpression();
                if (callNode.getName().content().equals("super") == false) {
                  throw new RuntimeException("The constructor for "+definition.getName()+" that takes "+constructor.requiredArgs()+
                      " arguments must first invoke a constructor to its parent, "+definition.getParent().getName());
                }
                else {
                  if (superConstructors.containsKey(callNode.arguments().length) == false) {
                    throw new RuntimeException("The constructor for "+definition.getName()+" that takes "+constructor.requiredArgs()+
                        " arguments must first invoke a constructor to its parent, "+definition.getParent().getName());
                  }
                }
              }
            }
          }
        }
        
        allDefs.add(definition);
      }
    }
    
    
    for(TypeDefinition def: allDefs) {
      if (def.getParent().isChildOf(def)) {
        throw new RuntimeException("Type Error! "+def.getName()+" is a child of "+def.getParent().getName());
      }
    }
    
  }

  /**
   * Invokes the main function of this Wordy program
   * @param file - the file name whose main function to invoke
   * @param argc - the amount of arguments this function accepts
   * @param constants - the arguments to pass to the main function
   * 
   * @return the return value of the main function, or null if no return
   */
  public Object execute(String file, int argc,  Instance ... constants) {
    if (runtimeInitialized == false) {
      throw new RuntimeException("Runtime hasn't been initialized!");
    }
    else if (constants.length != argc) {
      throw new RuntimeException("The amount of arguments given doesn't match the "
                                + "amount of arguments the main function accepts");
    }
    else {
      FileInstance fileInstance = files.get(file);
      if (fileInstance == null) {
        throw new RuntimeException("Cannot find the file '"+file+"' !");
      }
      
      Callable main = fileInstance.getDefinition().findFunction("main", argc).get(0);
      if (main == null) {
        throw new RuntimeException("The file '"+file+"' doesn't contain a main function "+
                                   "that takes in "+argc+" arguments");
      }
      
      RuntimeFile orgFile = (RuntimeFile) fileInstance.getDefinition();
            
      Map [] varMaps = {orgFile.getVariables()};
      Map [] funcMaps = {orgFile.getFunctions(), fileInstance.getDefinition().getJavaConstructors()};
      
      //System.out.println(">INITIALIZE: "+varMaps[0].size());
      
      RuntimeTable table = new RuntimeTable(varMaps, funcMaps, orgFile.getJavaClassMap() );
      GenVisitor visitor = new GenVisitor(table, fileInstance, this);
      
      try {
        Instance ret = main.call(visitor, table, constants);
        if (ret != null) {
          if (ret instanceof JavaInstance) {
            JavaInstance instance = (JavaInstance) ret;
            return instance.getInstance();
          }
        }
        return ret;
        
      } catch (InvocationException e) {
        e.printStackTrace();
        System.exit(0);
      } catch (FatalInternalException e) {
        /*
         * If fatal exception, just print the exception message and exit the runtime
         */
        System.err.println(e.getMessage());
        e.printStackTrace();
        System.exit(0);
      } 
      
      return null;
    }
  }
  
  public FileInstance findFile(String name) {
    return files.get(name);
  }
  
  public TypeDefinition findTypeDef(String fileName, String className) {
    if (files.containsKey(fileName)) {
      return files.get(fileName).getDefinition().getTypeDefs().get(className);
    }
    return null;
  }
}
